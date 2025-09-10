package com.noxcrew.noxesium.api.network.handshake.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import java.util.Map;
import net.kyori.adventure.key.Key;

/**
 * Sent to the client after handshaking to populate a registry already filled with entries
 * by the client with the appropriate server-indicated identifiers.
 */
public record ClientboundRegistryIdsUpdatePacket(int id, boolean reset, Key registry, Map<Key, Integer> ids)
        implements NoxesiumPacket {}
