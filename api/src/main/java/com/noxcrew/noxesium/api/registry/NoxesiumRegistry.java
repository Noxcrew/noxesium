package com.noxcrew.noxesium.api.registry;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A registry used by Noxesium for its different types of custom components. Registries
 * hold keys identified by a resource location, which is assigned an integer id, which
 * maps to a generic typed value.
 */
public class NoxesiumRegistry<T> {
    protected final Map<Integer, T> byId = new ConcurrentHashMap<>();
    protected final Map<Key, T> byKey = new ConcurrentHashMap<>();

    /**
     * Fully clears and resets this registry.
     */
    public void reset() {
        byId.clear();
        byKey.clear();
    }

    /**
     * Registers a new entry into this registry.
     */
    public <V extends T> V register(Key key, V value) {
        byKey.put(key, value);
        return value;
    }

    /**
     * Returns the value in this registry for the given id.
     */
    @Nullable
    public T getById(int id) {
        return byId.get(id);
    }

    /**
     * Returns the value in this registry for the given key.
     */
    @Nullable
    public T getByKey(Key key) {
        return byKey.get(key);
    }
}
