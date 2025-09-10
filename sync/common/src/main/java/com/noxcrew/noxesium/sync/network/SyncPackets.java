package com.noxcrew.noxesium.sync.network;

import static com.noxcrew.noxesium.api.network.PacketCollection.client;
import static com.noxcrew.noxesium.api.network.PacketCollection.server;

import com.noxcrew.noxesium.api.network.PacketCollection;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType;
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundEstablishSyncPacket;
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundRequestSyncPacket;
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundSyncFilePacket;
import com.noxcrew.noxesium.sync.network.serverbound.ServerboundRequestSyncPacket;
import com.noxcrew.noxesium.sync.network.serverbound.ServerboundSyncFilePacket;

/**
 * Defines the folder syncing protocol packets.
 */
public class SyncPackets {
    public static final PacketCollection INSTANCE = new PacketCollection();

    public static final NoxesiumPayloadType<ClientboundRequestSyncPacket> CLIENTBOUND_REQUEST_SYNC =
            client(INSTANCE, "clientbound_request_sync", ClientboundRequestSyncPacket.class);
    public static final NoxesiumPayloadType<ClientboundEstablishSyncPacket> CLIENTBOUND_ESTABLISH_SYNC =
            client(INSTANCE, "clientbound_establish_sync", ClientboundEstablishSyncPacket.class);
    public static final NoxesiumPayloadType<ClientboundSyncFilePacket> CLIENTBOUND_SYNC_FILE =
            client(INSTANCE, "clientbound_sync_file", ClientboundSyncFilePacket.class);

    public static final NoxesiumPayloadType<ServerboundRequestSyncPacket> SERVERBOUND_REQUEST_SYNC =
            server(INSTANCE, "serverbound_request_sync", ServerboundRequestSyncPacket.class);
    public static final NoxesiumPayloadType<ServerboundSyncFilePacket> SERVERBOUND_SYNC_FILE =
            server(INSTANCE, "serverbound_sync_file", ServerboundSyncFilePacket.class);
}
