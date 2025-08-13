package com.noxcrew.noxesium.api.fabric.network.handshake;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent back to the client with a list of entrypoints from the {@link ServerboundHandshakePacket}
 * that it knows about and wants to register.
 *
 * @see com.noxcrew.noxesium.api.fabric.NoxesiumEntrypoint for more information
 */
public record ClientboundHandshakeAcknowledgePacket(Map<String, String> entrypoints) implements NoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundHandshakeAcknowledgePacket> STREAM_CODEC =
            CustomPacketPayload.codec(
                    ClientboundHandshakeAcknowledgePacket::write, ClientboundHandshakeAcknowledgePacket::new);

    private ClientboundHandshakeAcknowledgePacket(RegistryFriendlyByteBuf buf) {
        this(buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readUtf));
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeMap(entrypoints, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return HandshakePackets.CLIENTBOUND_HANDSHAKE_ACKNOWLEDGE;
    }
}
