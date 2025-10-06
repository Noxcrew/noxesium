package com.noxcrew.noxesium.sync.network;

import static com.noxcrew.noxesium.api.network.PacketCollection.client;
import static com.noxcrew.noxesium.api.network.PacketCollection.server;

import com.noxcrew.noxesium.api.network.PacketCollection;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadGroup;
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundRequestFilePacket;
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundRequestSyncPacket;
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundSyncFilePacket;
import com.noxcrew.noxesium.sync.network.serverbound.ServerboundFileSystemPacket;
import com.noxcrew.noxesium.sync.network.serverbound.ServerboundRequestSyncPacket;
import com.noxcrew.noxesium.sync.network.serverbound.ServerboundSyncFilePacket;

/**
 * Defines the folder syncing protocol packets.
 */
public class SyncPackets {
    public static final PacketCollection INSTANCE = new PacketCollection();

    public static final NoxesiumPayloadGroup CLIENTBOUND_REQUEST_SYNC =
            client(INSTANCE, "clientbound_request_sync").add(ClientboundRequestSyncPacket.class);
    public static final NoxesiumPayloadGroup CLIENTBOUND_REQUEST_FILE =
            client(INSTANCE, "clientbound_request_file").add(ClientboundRequestFilePacket.class);
    public static final NoxesiumPayloadGroup CLIENTBOUND_SYNC_FILE =
            client(INSTANCE, "clientbound_sync_file").add(ClientboundSyncFilePacket.class);

    public static final NoxesiumPayloadGroup SERVERBOUND_REQUEST_SYNC =
            server(INSTANCE, "serverbound_request_sync").add(ServerboundRequestSyncPacket.class);
    public static final NoxesiumPayloadGroup SERVERBOUND_SYNC_FILE =
            server(INSTANCE, "serverbound_sync_file").add(ServerboundSyncFilePacket.class);
    public static final NoxesiumPayloadGroup SERVERBOUND_FILE_SYSTEM =
            server(INSTANCE, "serverbound_file_system").add(ServerboundFileSystemPacket.class);
}
