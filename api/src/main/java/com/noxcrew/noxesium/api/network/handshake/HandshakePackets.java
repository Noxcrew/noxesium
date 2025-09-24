package com.noxcrew.noxesium.api.network.handshake;

import static com.noxcrew.noxesium.api.network.PacketCollection.client;
import static com.noxcrew.noxesium.api.network.PacketCollection.server;

import com.noxcrew.noxesium.api.network.PacketCollection;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeCancelPacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeCompletePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeTransferredPacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundLazyPacketsPacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundRegistryContentUpdatePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundRegistryIdsUpdatePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakeCancelPacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundLazyPacketsPacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundRegistryUpdateResultPacket;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadGroup;

/**
 * Defines the handshake packets.
 */
public class HandshakePackets {
    public static final PacketCollection INSTANCE = new PacketCollection();

    public static final NoxesiumPayloadGroup SERVERBOUND_HANDSHAKE =
            server(INSTANCE, "serverbound_handshake").add(ServerboundHandshakePacket.class);
    public static final NoxesiumPayloadGroup SERVERBOUND_HANDSHAKE_ACKNOWLEDGE =
            server(INSTANCE, "serverbound_handshake_ack").add(ServerboundHandshakeAcknowledgePacket.class);
    public static final NoxesiumPayloadGroup SERVERBOUND_HANDSHAKE_CANCEL =
            server(INSTANCE, "serverbound_handshake_cancel").add(ServerboundHandshakeCancelPacket.class);
    public static final NoxesiumPayloadGroup SERVERBOUND_REGISTRY_UPDATE_RESULT =
            server(INSTANCE, "serverbound_registry_update_result").add(ServerboundRegistryUpdateResultPacket.class);
    public static final NoxesiumPayloadGroup SERVERBOUND_LAZY_PACKETS =
            server(INSTANCE, "serverbound_lazy_packets").add(ServerboundLazyPacketsPacket.class);

    public static final NoxesiumPayloadGroup CLIENTBOUND_HANDSHAKE_ACKNOWLEDGE =
            client(INSTANCE, "clientbound_handshake_ack").add(ClientboundHandshakeAcknowledgePacket.class);
    public static final NoxesiumPayloadGroup CLIENTBOUND_HANDSHAKE_COMPLETE =
            client(INSTANCE, "clientbound_handshake_complete").add(ClientboundHandshakeCompletePacket.class);
    public static final NoxesiumPayloadGroup CLIENTBOUND_HANDSHAKE_TRANSFERRED =
            client(INSTANCE, "clientbound_handshake_transferred").add(ClientboundHandshakeTransferredPacket.class);
    public static final NoxesiumPayloadGroup CLIENTBOUND_HANDSHAKE_CANCEL =
            client(INSTANCE, "clientbound_handshake_cancel").add(ClientboundHandshakeCancelPacket.class);
    public static final NoxesiumPayloadGroup CLIENTBOUND_REGISTRY_IDS_UPDATE =
            client(INSTANCE, "clientbound_registry_update_ids").add(ClientboundRegistryIdsUpdatePacket.class);
    public static final NoxesiumPayloadGroup CLIENTBOUND_REGISTRY_CONTENT_UPDATE =
            client(INSTANCE, "clientbound_registry_update_content").add(ClientboundRegistryContentUpdatePacket.class);
    public static final NoxesiumPayloadGroup CLIENTBOUND_LAZY_PACKETS =
            client(INSTANCE, "clientbound_lazy_packets").add(ClientboundLazyPacketsPacket.class);
}
