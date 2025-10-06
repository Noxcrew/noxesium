package com.noxcrew.noxesium.sync.network.serverbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import java.util.List;
import java.util.Map;

/**
 * Sent to the server to share what the file system looks like on the client.
 */
public record ServerboundFileSystemPacket(
        int syncId, int part, int total, Map<List<String>, Map<String, Long>> contents) implements NoxesiumPacket {}
