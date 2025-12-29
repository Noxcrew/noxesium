package com.noxcrew.noxesium.api.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import java.util.Collection;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

/**
 * A registry used by Noxesium for its different types of custom components. Registries
 * hold keys identified by a resource location, which is assigned an integer id, which
 * maps to a generic typed value.
 */
public class NoxesiumRegistry<T> {
    private final Key id;
    protected final BiMap<Integer, T> byId = HashBiMap.create();
    protected final BiMap<Key, T> byKey = HashBiMap.create();

    public NoxesiumRegistry(Key id) {
        this.id = id;
    }

    /**
     * Returns the id of this registry.
     */
    public Key id() {
        return id;
    }

    /**
     * Fully clears and resets this registry.
     */
    public void reset() {
        byId.clear();
        byKey.clear();
    }

    /**
     * Returns the keys of this registry.
     */
    public Collection<Key> getKeys() {
        return byKey.keySet();
    }

    /**
     * Returns all contents of this registry.
     */
    public Collection<T> getContents() {
        return byKey.values();
    }

    /**
     * Returns whether this registry contains the given key.
     */
    public boolean contains(Key key) {
        return byKey.containsKey(key);
    }

    /**
     * Returns the size of this registry.
     */
    public int size() {
        return byKey.size();
    }

    /**
     * Returns whether the registry is empty.
     */
    public boolean isEmpty() {
        return byKey.isEmpty();
    }

    /**
     * Registers a new entry into this registry.
     */
    public final <V extends T> V register(Key key, V value) {
        return register(key, value, null);
    }

    /**
     * Registers a new entry into this registry.
     */
    public <V extends T> V register(Key key, V value, @Nullable NoxesiumEntrypoint entrypoint) {
        if (byKey.get(key) != value) {
            byKey.put(key, value);
        }
        return value;
    }

    /**
     * Removes the given key from the registry.
     */
    public void remove(Key key) {
        byKey.remove(key);
    }

    /**
     * Returns the value in this registry for the given id.
     */
    @Nullable
    public T getById(int id) {
        return byId.get(id);
    }

    /**
     * Returns the index associated with the given value.
     * Returns -1 if the key is not in this registry.
     */
    public int getIdFor(T value) {
        if (value == null) return -1;
        var boxed = byId.inverse().get(value);
        return boxed == null ? -1 : boxed;
    }

    /**
     * Returns the index associated with the given key.
     * Returns -1 if the key is not in this registry.
     */
    public int getIdForKey(Key key) {
        if (key == null) return -1;
        var value = getByKey(key);
        if (value == null) return -1;
        return getIdFor(value);
    }

    /**
     * Returns the key associated with the given value.
     */
    @Nullable
    public Key getKeyFor(T value) {
        return byKey.inverse().get(value);
    }

    /**
     * Returns the value in this registry for the given key.
     */
    @Nullable
    public T getByKey(Key key) {
        return byKey.get(key);
    }
}
