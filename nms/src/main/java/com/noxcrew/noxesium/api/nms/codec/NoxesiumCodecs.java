package com.noxcrew.noxesium.api.nms.codec;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noxcrew.noxesium.api.component.NoxesiumComponentPatch;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.nms.registry.ComponentSerializerRegistry;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry;
import com.noxcrew.noxesium.api.util.Unit;
import com.noxcrew.noxesium.core.feature.item.HoverSound;
import com.noxcrew.noxesium.core.feature.item.Hoverable;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.key.Key;
import net.minecraft.util.ExtraCodecs;

/**
 * Defines extra codecs used by Noxesium.
 */
public class NoxesiumCodecs {

    public static final Codec<Unit> UNIT = Codec.unit(Unit.INSTANCE);

    public static final Codec<List<Integer>> INT_LIST = Codec.INT_STREAM.comapFlatMap(
            stream -> DataResult.success(stream.boxed().toList()),
            list -> list.stream().mapToInt(Integer::intValue));

    public static final Codec<Color> COLOR_ARGB =
            ExtraCodecs.ARGB_COLOR_CODEC.comapFlatMap(value -> DataResult.success(new Color(value)), Color::getRGB);

    public static final Codec<Key> KEY =
            Codec.STRING.comapFlatMap(string -> DataResult.success(Key.key(string)), Key::asString);

    public static Codec<Hoverable> HOVERABLE = RecordCodecBuilder.create((instance) -> instance.group(
                    Codec.BOOL.optionalFieldOf("hoverable", true).forGetter(Hoverable::hoverable),
                    KEY.optionalFieldOf("front_sprite").forGetter(Hoverable::frontSprite),
                    KEY.optionalFieldOf("back_sprite").forGetter(Hoverable::backSprite))
            .apply(instance, Hoverable::new));

    public static Codec<HoverSound.Sound> SOUND = RecordCodecBuilder.create((instance) -> instance.group(
                    KEY.fieldOf("sound").forGetter(HoverSound.Sound::sound),
                    Codec.FLOAT.optionalFieldOf("volume", 1f).forGetter(HoverSound.Sound::volume),
                    Codec.FLOAT.optionalFieldOf("pitch_min", 1f).forGetter(HoverSound.Sound::pitchMin),
                    Codec.FLOAT.optionalFieldOf("pitch_max", 1f).forGetter(HoverSound.Sound::pitchMax))
            .apply(instance, HoverSound.Sound::new));

    public static Codec<HoverSound> HOVER_SOUND = RecordCodecBuilder.create((instance) -> instance.group(
                    SOUND.optionalFieldOf("hover_on").forGetter(HoverSound::hoverOn),
                    SOUND.optionalFieldOf("hover_off").forGetter(HoverSound::hoverOff),
                    Codec.BOOL
                            .optionalFieldOf("only_play_in_non_player_inventories", false)
                            .forGetter(HoverSound::onlyPlayInNonPlayerInventories))
            .apply(instance, HoverSound::new));

    public static <E extends Enum<?>> Codec<E> forEnum(Class<E> clazz) {
        return new PrimitiveCodec<>() {
            @Override
            public <T> DataResult<E> read(final DynamicOps<T> ops, final T input) {
                return ops.getNumberValue(input).map(number -> clazz.getEnumConstants()[number.intValue()]);
            }

            @Override
            public <T> T write(final DynamicOps<T> ops, final E value) {
                return ops.createByte((byte) value.ordinal());
            }

            @Override
            public String toString() {
                return clazz.getSimpleName();
            }
        };
    }

    public static Codec<NoxesiumComponentPatch> noxesiumComponentPatch(
            NoxesiumRegistry<NoxesiumComponentType<?>> registry) {
        var componentTypeCodec = Codec.STRING.<NoxesiumComponentType<?>>flatXmap(
                key -> {
                    var resource = Key.key(key);
                    var componentType = registry.getByKey(resource);
                    if (componentType == null) {
                        return DataResult.error(() -> "No component with type: '" + resource + "'");
                    } else {
                        return DataResult.success(componentType);
                    }
                },
                type -> DataResult.success(type.id().toString()));

        return Codec.<NoxesiumComponentType<?>, Object>dispatchedMap(componentTypeCodec, type -> {
                    var serializer = ComponentSerializerRegistry.getSerializers(registry, type);
                    Preconditions.checkNotNull(
                            serializer, "Could not find serializer for component with type: '" + type.id() + "'");
                    return serializer.codec();
                })
                .xmap(
                        map -> {
                            if (map.isEmpty()) {
                                return new NoxesiumComponentPatch(new ConcurrentHashMap<>());
                            } else {
                                var patch = new ConcurrentHashMap<NoxesiumComponentType<?>, Optional<?>>(map.size());
                                for (var entry : map.entrySet()) {
                                    patch.put(entry.getKey(), Optional.ofNullable(entry.getValue()));
                                }
                                return new NoxesiumComponentPatch(patch);
                            }
                        },
                        patch -> {
                            if (patch.isEmpty()) {
                                return new HashMap<>();
                            } else {
                                var map = new ConcurrentHashMap<NoxesiumComponentType<?>, Object>(
                                        patch.getMap().size());
                                for (var entry : patch.getMap().entrySet()) {
                                    // Ignore empty values in patches when serializing! Empty values are used only in
                                    // the stream codec to indicate values to remove. The codec is used for saving to
                                    // the disk on the server-side where we only save the actual values.
                                    if (entry.getValue().isEmpty()) continue;
                                    map.put(entry.getKey(), entry.getValue().orElseThrow());
                                }
                                return map;
                            }
                        });
    }
}
