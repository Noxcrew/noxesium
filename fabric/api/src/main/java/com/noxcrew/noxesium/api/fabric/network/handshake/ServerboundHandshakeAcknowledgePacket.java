package com.noxcrew.noxesium.api.fabric.network.handshake;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.api.fabric.network.ServerboundNoxesiumPacket;
import java.util.Collection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent back to the server with a list of entrypoints from the {@link ClientboundHandshakeAcknowledgePacket}
 * and their protocol information that have been registered.
 *
 * @see com.noxcrew.noxesium.api.fabric.NoxesiumEntrypoint for more information
 */
public record ServerboundHandshakeAcknowledgePacket(Collection<EntrypointProtocol> protocols)
        implements ServerboundNoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundHandshakeAcknowledgePacket> STREAM_CODEC =
            CustomPacketPayload.codec(
                    ServerboundHandshakeAcknowledgePacket::write, ServerboundHandshakeAcknowledgePacket::new);

    private ServerboundHandshakeAcknowledgePacket(RegistryFriendlyByteBuf buf) {
        this(buf.readList(EntrypointProtocol.STREAM_CODEC));
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeCollection(protocols, EntrypointProtocol.STREAM_CODEC);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return HandshakePackets.SERVERBOUND_HANDSHAKE_ACKNOWLEDGE;
    }
}
