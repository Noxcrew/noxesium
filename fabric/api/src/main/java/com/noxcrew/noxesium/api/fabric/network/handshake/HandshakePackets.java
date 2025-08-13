package com.noxcrew.noxesium.api.fabric.network.handshake;

import static com.noxcrew.noxesium.api.fabric.network.PacketCollection.client;
import static com.noxcrew.noxesium.api.fabric.network.PacketCollection.server;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.api.fabric.network.PacketCollection;

/**
 * Defines the handshake packets.
 */
public class HandshakePackets {
    public static final PacketCollection INSTANCE = new PacketCollection();

    public static final NoxesiumPayloadType<ServerboundHandshakePacket> SERVERBOUND_HANDSHAKE =
            server(INSTANCE, "serverbound_handshake", ServerboundHandshakePacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ServerboundHandshakeAcknowledgePacket> SERVERBOUND_HANDSHAKE_ACKNOWLEDGE =
            server(INSTANCE, "serverbound_handshake_ack", ServerboundHandshakeAcknowledgePacket.STREAM_CODEC);

    public static final NoxesiumPayloadType<ClientboundHandshakeAcknowledgePacket> CLIENTBOUND_HANDSHAKE_ACKNOWLEDGE =
            client(INSTANCE, "clientbound_handshake_ack", ClientboundHandshakeAcknowledgePacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundRegistryIdentifiersPacket> CLIENTBOUND_REGISTRY_IDS =
            client(INSTANCE, "clientbound_registry_ids", ClientboundRegistryIdentifiersPacket.STREAM_CODEC);
}
