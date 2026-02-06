package com.noxcrew.noxesium.api.registry;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

/**
 * A registry used by Noxesium for its different types of custom components. Registries
 * hold keys identified by a resource location, which is assigned an integer id, which
 * maps to a generic typed value.
 */
public class NoxesiumRegistry<T> {
    private final Key id;
    protected final Map<Integer, T> idToValue = new HashMap<>();
    protected final Map<T, Integer> valueToId = new HashMap<>();
    protected final Map<Key, T> keyToValue = new HashMap<>();
    protected final Map<T, Key> valueToKey = new HashMap<>();

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
        idToValue.clear();
        valueToId.clear();
        keyToValue.clear();
        valueToKey.clear();
    }

    /**
     * Returns the keys of this registry.
     */
    public Collection<Key> getKeys() {
        return keyToValue.keySet();
    }

    /**
     * Returns all contents of this registry.
     */
    public Collection<T> getContents() {
        return valueToKey.keySet();
    }

    /**
     * Returns whether this registry contains the given key.
     */
    public boolean contains(Key key) {
        return keyToValue.containsKey(key);
    }

    /**
     * Returns the size of this registry.
     */
    public int size() {
        return keyToValue.size();
    }

    /**
     * Returns whether the registry is empty.
     */
    public boolean isEmpty() {
        return keyToValue.isEmpty();
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
        if (keyToValue.get(key) != value) {
            var oldValue = keyToValue.put(key, value);
            if (oldValue != null) {
                valueToKey.remove(oldValue);
            }
            valueToKey.put(value, key);
        }
        return value;
    }

    /**
     * Removes the given key from the registry.
     */
    public void remove(Key key) {
        var oldValue = keyToValue.remove(key);
        if (oldValue != null) {
            valueToKey.remove(oldValue);
        }
    }

    /**
     * Returns the value in this registry for the given id.
     */
    @Nullable
    public T getById(int id) {
        return idToValue.get(id);
    }

    /**
     * Returns the index associated with the given value.
     * Returns -1 if the key is not in this registry.
     */
    public int getIdFor(T value) {
        if (value == null) return -1;
        var boxed = valueToId.get(value);
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
        return valueToKey.get(value);
    }

    /**
     * Returns the value in this registry for the given key.
     */
    @Nullable
    public T getByKey(Key key) {
        return keyToValue.get(key);
    }
}
