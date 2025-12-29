package com.noxcrew.noxesium.api.nms.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noxcrew.noxesium.api.feature.qib.QibDefinition;
import com.noxcrew.noxesium.core.feature.GuiConstraints;
import com.noxcrew.noxesium.api.util.Unit;
import com.noxcrew.noxesium.core.feature.item.HoverSound;
import com.noxcrew.noxesium.core.feature.item.Hoverable;
import java.awt.Color;
import java.util.List;
import net.kyori.adventure.key.Key;
import net.minecraft.util.ExtraCodecs;

/**
 * Defines extra codecs used by Noxesium.
 */
public class NoxesiumCodecs {

    public static final Codec<Unit> UNIT = MapCodec.unitCodec(Unit.INSTANCE);

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

    public static Codec<GuiConstraints> GUI_CONSTRAINTS = RecordCodecBuilder.create((instance) -> instance.group(
                    Codec.DOUBLE.fieldOf("scalar").forGetter(GuiConstraints::scalar),
                    Codec.DOUBLE.optionalFieldOf("minValue", 0.001).forGetter(GuiConstraints::minValue),
                    Codec.DOUBLE.optionalFieldOf("maxValue", 10.0).forGetter(GuiConstraints::maxValue))
            .apply(instance, GuiConstraints::new));

    public static final Codec<QibDefinition> QIB_DEFINITION = Codec.STRING.comapFlatMap(
            string -> DataResult.success(QibDefinition.QIB_GSON.fromJson(string, QibDefinition.class)),
            QibDefinition.QIB_GSON::toJson);

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
}
