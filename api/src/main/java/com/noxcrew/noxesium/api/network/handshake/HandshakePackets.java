package com.noxcrew.noxesium.api.network.handshake;

import static com.noxcrew.noxesium.api.network.PacketCollection.client;
import static com.noxcrew.noxesium.api.network.PacketCollection.server;

import com.noxcrew.noxesium.api.network.PacketCollection;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeCancelPacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeCompletePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundRegistryContentUpdatePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundRegistryIdsUpdatePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakeCancelPacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundRegistryUpdateResultPacket;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType;

/**
 * Defines the handshake packets.
 */
public class HandshakePackets {
    public static final PacketCollection INSTANCE = new PacketCollection();

    public static final NoxesiumPayloadType<ServerboundHandshakePacket> SERVERBOUND_HANDSHAKE =
            server(INSTANCE, "serverbound_handshake", ServerboundHandshakePacket.class);
    public static final NoxesiumPayloadType<ServerboundHandshakeAcknowledgePacket> SERVERBOUND_HANDSHAKE_ACKNOWLEDGE =
            server(INSTANCE, "serverbound_handshake_ack", ServerboundHandshakeAcknowledgePacket.class);
    public static final NoxesiumPayloadType<ServerboundHandshakeCancelPacket> SERVERBOUND_HANDSHAKE_CANCEL =
            server(INSTANCE, "serverbound_handshake_cancel", ServerboundHandshakeCancelPacket.class);
    public static final NoxesiumPayloadType<ServerboundRegistryUpdateResultPacket> SERVERBOUND_REGISTRY_UPDATE_RESULT =
            server(INSTANCE, "serverbound_registry_update_result", ServerboundRegistryUpdateResultPacket.class);

    public static final NoxesiumPayloadType<ClientboundHandshakeAcknowledgePacket> CLIENTBOUND_HANDSHAKE_ACKNOWLEDGE =
            client(INSTANCE, "clientbound_handshake_ack", ClientboundHandshakeAcknowledgePacket.class);
    public static final NoxesiumPayloadType<ClientboundHandshakeCompletePacket> CLIENTBOUND_HANDSHAKE_COMPLETE =
            client(INSTANCE, "clientbound_handshake_complete", ClientboundHandshakeCompletePacket.class);
    public static final NoxesiumPayloadType<ClientboundHandshakeCancelPacket> CLIENTBOUND_HANDSHAKE_CANCEL =
            client(INSTANCE, "clientbound_handshake_cancel", ClientboundHandshakeCancelPacket.class);
    public static final NoxesiumPayloadType<ClientboundRegistryIdsUpdatePacket> CLIENTBOUND_REGISTRY_IDS_UPDATE =
            client(INSTANCE, "clientbound_registry_update_ids", ClientboundRegistryIdsUpdatePacket.class);
    public static final NoxesiumPayloadType<ClientboundRegistryContentUpdatePacket>
            CLIENTBOUND_REGISTRY_CONTENT_UPDATE = client(
                    INSTANCE, "clientbound_registry_update_content", ClientboundRegistryContentUpdatePacket.class);
}
