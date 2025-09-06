package com.noxcrew.noxesium.sync.network;

import com.noxcrew.noxesium.api.network.PacketCollection;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType;
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundRequestSyncEvent;

/**
 * Defines the folder syncing protocol packets.
 */
public class SyncPackets {
    public static final PacketCollection INSTANCE = new PacketCollection();

    public static final NoxesiumPayloadType<ClientboundRequestSyncEvent> CLIENTBOUND_REQUEST_SYNC =
            PacketCollection.client(INSTANCE, "clientbound_request_sync", ClientboundRequestSyncEvent.class);
}
