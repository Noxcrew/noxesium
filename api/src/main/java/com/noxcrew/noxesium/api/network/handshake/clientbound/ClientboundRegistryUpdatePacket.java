package com.noxcrew.noxesium.api.network.handshake.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import java.util.Map;
import net.kyori.adventure.key.Key;

/**
 * Sent to the client after handshaking to populate a registry using client-known identifiers.
 */
public record ClientboundRegistryUpdatePacket(int id, Key registry, Map<Integer, Key> ids) implements NoxesiumPacket {}
