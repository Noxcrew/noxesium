package com.noxcrew.noxesium.api.fabric.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.noxcrew.noxesium.api.component.NoxesiumComponentPatch;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry;
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

    public static final Codec<List<Integer>> INT_LIST = Codec.INT_STREAM.comapFlatMap(
            stream -> DataResult.success(stream.boxed().toList()),
            list -> list.stream().mapToInt(Integer::intValue));

    public static final Codec<Color> COLOR_ARGB =
            ExtraCodecs.ARGB_COLOR_CODEC.comapFlatMap(value -> DataResult.success(new Color(value)), Color::getRGB);

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

    public static Codec<NoxesiumComponentPatch> noxesiumComponentPatch(NoxesiumRegistry<NoxesiumComponentType<?>> registry) {
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

        return Codec.<NoxesiumComponentType<?>, Object>dispatchedMap(componentTypeCodec, NoxesiumComponentType::codec)
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
