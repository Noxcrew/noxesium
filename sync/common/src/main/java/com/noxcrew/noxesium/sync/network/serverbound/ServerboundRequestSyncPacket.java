package com.noxcrew.noxesium.sync.network.serverbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;

/**
 * Sent to the server to indicate a client wants to start a new synchronization
 * session for the given folder id.
 */
public record ServerboundRequestSyncPacket(String id, int syncId) implements NoxesiumPacket {}
