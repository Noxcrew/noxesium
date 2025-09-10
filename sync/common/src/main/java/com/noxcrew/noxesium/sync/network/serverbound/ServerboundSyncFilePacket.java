package com.noxcrew.noxesium.sync.network.serverbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.sync.network.SyncedPart;

/**
 * Sent to the server with the contents of a file. Contains information on which part out of the total this is.
 */
public record ServerboundSyncFilePacket(int syncId, SyncedPart part) implements NoxesiumPacket {
}
