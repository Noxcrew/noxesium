package com.noxcrew.noxesium.api.registry;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.kyori.adventure.key.Key;

/**
 * Stores a collection of components to be ingested by a client into a serializable registry.
 */
public class NoxesiumRegistryPatch {
    private final Key registry;
    private final Map<Key, Optional<?>> data;
    private final Map<Key, Integer> keys;

    public NoxesiumRegistryPatch(Key registry, Map<Key, Optional<?>> data, Map<Key, Integer> keys) {
        this.registry = registry;
        this.data = data;
        this.keys = keys;
    }

    /**
     * Returns the registry id this patch is for.
     */
    public Key getRegistry() {
        return registry;
    }

    /**
     * Returns whether this patch is empty.
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Returns the raw contents of this patch.
     */
    public Map<Key, Optional<?>> getMap() {
        return data;
    }

    /**
     * Returns all ids in this patch.
     */
    public Collection<Integer> getIds() {
        return keys.values();
    }

    /**
     * Returns the new identifier keys for the synchronized content.
     */
    public Map<Key, Integer> getKeys() {
        return keys;
    }

    @Override
    public String toString() {
        return Objects.toString(data);
    }
}
