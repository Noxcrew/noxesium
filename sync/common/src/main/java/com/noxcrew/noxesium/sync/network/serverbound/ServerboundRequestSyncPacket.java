package com.noxcrew.noxesium.sync.network.serverbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import java.util.Map;

/**
 * Sent to the server to indicate a client wants to start a new synchronization
 * session for the given folder id with the given files as a starting point.
 */
public record ServerboundRequestSyncPacket(String id, int syncId, Map<String, Long> files) implements NoxesiumPacket {}
