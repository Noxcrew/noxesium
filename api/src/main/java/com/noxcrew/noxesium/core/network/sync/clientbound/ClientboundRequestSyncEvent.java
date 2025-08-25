package com.noxcrew.noxesium.core.network.sync.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;

/**
 * Sent to the client to indicate a folder can be synced with the given id.
 */
public record ClientboundRequestSyncEvent(String id) implements NoxesiumPacket {}
