package com.noxcrew.noxesium.api.network;

import java.util.Collection;
import net.kyori.adventure.key.Key;

/**
 * Stores information on an entrypoint's protocol.
 */
public record EntrypointProtocol(String id, String version, Collection<Key> capabilities) {}
