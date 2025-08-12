package com.noxcrew.noxesium.api.fabric.network.handshake;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.api.fabric.network.PacketCollection;

/**
 * Defines the handshake packets.
 */
public class HandshakePackets extends PacketCollection {
    /**
     * The instance of the handshake packet collection.
     */
    public static final HandshakePackets INSTANCE = new HandshakePackets();

    public final NoxesiumPayloadType<ClientboundHandshakeAcknowledgePacket> CLIENTBOUND_HANDSHAKE_ACKNOWLEDGE =
            client("clientbound_handshake_ack", ClientboundHandshakeAcknowledgePacket.STREAM_CODEC);
    public final NoxesiumPayloadType<ClientboundRegistryIdentifiersPacket> CLIENTBOUND_REGISTRY_IDS =
            client("clientbound_registry_ids", ClientboundRegistryIdentifiersPacket.STREAM_CODEC);

    public final NoxesiumPayloadType<ServerboundHandshakePacket> SERVERBOUND_HANDSHAKE =
            server("serverbound_handshake", ServerboundHandshakePacket.STREAM_CODEC);
    public final NoxesiumPayloadType<ServerboundHandshakeAcknowledgePacket> SERVERBOUND_HANDSHAKE_ACKNOWLEDGE =
            server("serverbound_handshake_ack", ServerboundHandshakeAcknowledgePacket.STREAM_CODEC);
}
