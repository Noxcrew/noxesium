package com.noxcrew.noxesium.api.registry;

import java.util.LinkedHashMap;
import java.util.Map;
import net.kyori.adventure.key.Key;

/**
 * A collection of registry entries that can be (re-)registered as a group.
 */
public final class RegistryCollection<T> {
    private final NoxesiumRegistry<T> registry;
    private final Map<Key, T> entries = new LinkedHashMap<>();

    public RegistryCollection(NoxesiumRegistry<T> registry) {
        this.registry = registry;
    }

    /**
     * Adds a new registration.
     */
    public T register(Key key, T value) {
        entries.put(key, value);
        return value;
    }

    /**
     * Registers all registry entries.
     */
    public void register() {
        for (var entry : entries.entrySet()) {
            registry.register(entry.getKey(), entry.getValue());
        }
    }
}
