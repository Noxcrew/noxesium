package com.noxcrew.noxesium.api.registry;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

/**
 * A variant of the server-side Noxesium registry that tracks which contents have to be
 * re-synchronized with the client.
 */
public class SynchronizedServerNoxesiumRegistry<T> extends ServerNoxesiumRegistry<T> {

    /**
     * All fields which have to be synchronized with all clients.
     */
    private final Set<Key> pendingUpdates = ConcurrentHashMap.newKeySet();

    public SynchronizedServerNoxesiumRegistry(Key id) {
        super(id);
    }

    /**
     * Returns all data of this registry to sync with clients for the given entrypoints.
     */
    public NoxesiumRegistryPatch determineAllSyncableContent(List<String> entrypoints) {
        var data = new HashMap<Key, Optional<?>>();
        var keys = new HashMap<Key, Integer>();
        for (var entry : this.entrypoints.entrySet()) {
            // Ignore keys for entrypoints this player does not have know about!
            if (entry.getValue() != null && !entrypoints.contains(entry.getValue())) continue;

            // Determine the id of this key
            var value = getByKey(entry.getKey());
            var index = getIdFor(value);
            data.put(entry.getKey(), Optional.ofNullable(value));
            keys.put(entry.getKey(), index);
        }
        return new NoxesiumRegistryPatch(id(), data, keys);
    }

    /**
     * Returns all data of this registry that has changed.
     */
    public NoxesiumRegistryPatch determineAllChangedContent(List<String> entrypoints) {
        var data = new HashMap<Key, Optional<?>>();
        var keys = new HashMap<Key, Integer>();
        for (var entry : this.entrypoints.entrySet()) {
            // Ignore keys that are not dirty
            if (!pendingUpdates.contains(entry.getKey())) continue;

            // Ignore keys for entrypoints this player does not have know about!
            if (entry.getValue() != null && !entrypoints.contains(entry.getValue())) continue;

            // Determine the id of this key
            var value = getByKey(entry.getKey());
            var index = getIdFor(value);
            data.put(entry.getKey(), Optional.ofNullable(value));
            keys.put(entry.getKey(), index);
        }
        return new NoxesiumRegistryPatch(id(), data, keys);
    }

    /**
     * Returns a list of all pending updates.
     */
    public Set<Key> getPendingUpdates() {
        return pendingUpdates;
    }

    /**
     * Clears all pending updates.
     */
    public void clearPendingUpdates() {
        pendingUpdates.clear();
    }

    /**
     * Returns whether any updates are pending.
     */
    public boolean isDirty() {
        return !pendingUpdates.isEmpty();
    }

    @Override
    public void remove(Key key) {
        if (byKey.containsKey(key)) {
            pendingUpdates.add(key);
        }
        super.remove(key);
    }

    @Override
    public <V extends T> V register(Key key, V value, @Nullable NoxesiumEntrypoint entrypoint) {
        var result = super.register(key, value, entrypoint);
        pendingUpdates.add(key);
        return result;
    }

    @Override
    public void reset() {
        pendingUpdates.addAll(byKey.keySet());
        super.reset();
    }
}
