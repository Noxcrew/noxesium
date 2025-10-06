package com.noxcrew.noxesium.sync.network.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import java.util.Collection;

/**
 * Sent to the client to indicate a file is requested to be synced to the server.
 */
public record ClientboundRequestFilePacket(int syncId, Collection<String> requestedFiles) implements NoxesiumPacket {}
