package com.noxcrew.noxesium.api.fabric.registry;

import io.netty.buffer.ByteBuf;
import java.awt.Color;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Defines extra stream codecs used by Noxesium.
 */
public class NoxesiumExtraStreamCodecs {

    public static final StreamCodec<FriendlyByteBuf, Integer> COLOR_INT_ARGB = new StreamCodec<>() {
        public Integer decode(FriendlyByteBuf buffer) {
            return buffer.readVarInt();
        }

        public void encode(FriendlyByteBuf buffer, Integer value) {
            buffer.writeVarInt(value);
        }
    };

    public static final StreamCodec<FriendlyByteBuf, Color> COLOR_ARGB = COLOR_INT_ARGB.map(Color::new, Color::getRGB);

    /**
     * Creates a new stream codec for the given enum class.
     */
    public static <T extends Enum<?>> StreamCodec<ByteBuf, T> forEnum(Class<T> clazz) {
        return new StreamCodec<>() {
            @Override
            public void encode(ByteBuf buffer, T value) {
                buffer.writeByte(value.ordinal());
            }

            @Override
            public T decode(ByteBuf buffer) {
                return clazz.getEnumConstants()[buffer.readByte()];
            }
        };
    }
}
