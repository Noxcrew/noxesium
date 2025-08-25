package com.noxcrew.noxesium.api.registry;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import java.util.LinkedHashMap;
import java.util.Map;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

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
    public void register(@Nullable NoxesiumEntrypoint entrypoint) {
        for (var entry : entries.entrySet()) {
            registry.register(entry.getKey(), entry.getValue(), entrypoint);
        }
    }
}
