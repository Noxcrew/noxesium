package com.noxcrew.noxesium.api.fabric.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import java.awt.Color;
import java.util.List;
import net.minecraft.util.ExtraCodecs;

/**
 * Defines extra codecs used by Noxesium.
 */
public class NoxesiumExtraCodecs {

    public static final Codec<List<Integer>> INT_LIST = Codec.INT_STREAM.comapFlatMap(
            stream -> DataResult.success(stream.boxed().toList()),
            list -> list.stream().mapToInt(Integer::intValue));

    public static final Codec<Color> COLOR_ARGB =
            ExtraCodecs.ARGB_COLOR_CODEC.comapFlatMap(value -> DataResult.success(new Color(value)), Color::getRGB);

    /**
     * Creates a new codec for the given enum class.
     */
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
