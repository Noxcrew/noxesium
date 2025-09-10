package com.noxcrew.noxesium.api.player;

import com.noxcrew.noxesium.api.registry.NoxesiumRegistry;
import java.util.Collection;

/**
 * Stores a set of id changes.
 */
public record IdChangeSet(
        NoxesiumRegistry<?> registry, boolean reset, Collection<Integer> added, Collection<Integer> removed) {}
