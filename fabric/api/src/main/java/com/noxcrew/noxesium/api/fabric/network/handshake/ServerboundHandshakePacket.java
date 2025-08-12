package com.noxcrew.noxesium.api.fabric.network.handshake;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.api.fabric.network.ServerboundNoxesiumPacket;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent to the server with a set of entrypoints that are available.
 *
 * @see com.noxcrew.noxesium.api.fabric.NoxesiumEntrypoint for more information
 */
public record ServerboundHandshakePacket(Map<String, String> entrypoints) implements ServerboundNoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundHandshakePacket> STREAM_CODEC =
            CustomPacketPayload.codec(ServerboundHandshakePacket::write, ServerboundHandshakePacket::new);

    private ServerboundHandshakePacket(RegistryFriendlyByteBuf buf) {
        this(buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readUtf));
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeMap(entrypoints, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return HandshakePackets.INSTANCE.SERVERBOUND_HANDSHAKE;
    }
}
