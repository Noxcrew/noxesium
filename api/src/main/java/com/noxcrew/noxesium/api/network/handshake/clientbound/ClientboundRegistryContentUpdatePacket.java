package com.noxcrew.noxesium.api.network.handshake.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistryPatch;

/**
 * Sent to the client after handshaking to populate a serializable registry
 * with new content.
 */
public record ClientboundRegistryContentUpdatePacket(int id, boolean reset, NoxesiumRegistryPatch patch)
        implements NoxesiumPacket {}
