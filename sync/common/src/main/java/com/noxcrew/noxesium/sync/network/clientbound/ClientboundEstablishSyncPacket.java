package com.noxcrew.noxesium.sync.network.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import java.util.Collection;

/**
 * Sent to the client to indicate a synchronization has been accepted and the client can send across
 * the requested files.
 */
public record ClientboundEstablishSyncPacket(int syncId, Collection<String> requestedFiles) implements NoxesiumPacket {}
