package com.noxcrew.noxesium.api.registry;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

/**
 * A registry used by Noxesium for its different types of custom components. Registries
 * hold keys identified by a resource location, which is assigned an integer id, which
 * maps to a generic typed value.
 */
public class NoxesiumRegistry<T> {
    public static class Entry<T> {
        private final Key key;
        private final T value;
        private int id = -1;

        public Entry(Key key, T value) {
            this.key = key;
            this.value = value;
        }

        public Key key() {
            return key;
        }

        public T value() {
            return value;
        }

        public int id() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    private final Key id;
    protected final Map<Integer, Entry<T>> idToEntry = new HashMap<>();
    protected final Map<Key, Entry<T>> keyToEntry = new HashMap<>();
    protected final Map<T, List<Entry<T>>> valueToEntries = new HashMap<>();

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
        idToEntry.clear();
        keyToEntry.clear();
        valueToEntries.clear();
    }

    /**
     * Returns the keys of this registry.
     */
    public Collection<Key> getKeys() {
        return keyToEntry.keySet();
    }

    /**
     * Returns all contents of this registry.
     */
    public Collection<T> getContents() {
        return valueToEntries.keySet();
    }

    /**
     * Returns whether this registry contains the given key.
     */
    public boolean contains(Key key) {
        return keyToEntry.containsKey(key);
    }

    /**
     * Returns the size of this registry.
     */
    public int size() {
        return keyToEntry.size();
    }

    /**
     * Returns whether the registry is empty.
     */
    public boolean isEmpty() {
        return keyToEntry.isEmpty();
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
        var entry = keyToEntry.get(key);
        if (entry != null && entry.value() != value) {
            remove(key);
            entry = null;
        }
        if (entry == null) {
            entry = new Entry<>(key, value);
            keyToEntry.put(key, entry);
            valueToEntries.computeIfAbsent(value, k -> new ArrayList<>()).add(entry);
        }
        return value;
    }

    /**
     * Removes the given key from the registry.
     */
    public void remove(Key key) {
        var entry = keyToEntry.remove(key);
        if (entry != null) {
            if (entry.id() != -1) idToEntry.remove(entry.id());
            var list = valueToEntries.get(entry.value());
            if (list != null) {
                list.remove(entry);
                if (list.isEmpty()) valueToEntries.remove(entry.value());
            }
        }
    }

    /**
     * Returns the value in this registry for the given id.
     */
    @Nullable
    public T getById(int id) {
        var entry = idToEntry.get(id);
        return entry == null ? null : entry.value();
    }

    /**
     * Returns the index associated with the given value.
     * Returns -1 if the key is not in this registry.
     */
    public int getIdFor(T value) {
        if (value == null) return -1;
        var list = valueToEntries.get(value);
        return list == null || list.isEmpty() ? -1 : list.get(0).id();
    }

    /**
     * Returns the index associated with the given key.
     * Returns -1 if the key is not in this registry.
     */
    public int getIdForKey(Key key) {
        if (key == null) return -1;
        var entry = keyToEntry.get(key);
        return entry == null ? -1 : entry.id();
    }

    /**
     * Returns the key associated with the given id.
     */
    @Nullable
    public Key getKeyForId(int id) {
        var entry = idToEntry.get(id);
        return entry == null ? null : entry.key();
    }

    /**
     * Returns the value in this registry for the given key.
     */
    @Nullable
    public T getByKey(Key key) {
        var entry = keyToEntry.get(key);
        return entry == null ? null : entry.value();
    }
}
