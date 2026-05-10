package com.noxcrew.noxesium.api.registry;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    /**
     * Tracks removed keys and their old indices so that removal updates can be sent
     * even after the key is gone from {@link #entrypoints}.
     */
    private final Map<Key, Integer> pendingRemovals = new HashMap<>();

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
            var index = getIdForKey(entry.getKey());
            data.put(entry.getKey(), Optional.ofNullable(value));
            keys.put(entry.getKey(), index);
        }
        return new NoxesiumRegistryPatch(id(), data, keys);
    }

    /**
     * Returns all data of this registry that has changed.
     */
    public NoxesiumRegistryPatch determineAllChangedContent(List<String> playerEntrypoints) {
        var data = new HashMap<Key, Optional<?>>();
        var keys = new HashMap<Key, Integer>();
        for (var dirtyKey : pendingUpdates) {
            // Ignore keys for entrypoints this player does not know about
            var entrypointId = this.entrypoints.get(dirtyKey);
            if (entrypointId != null && !playerEntrypoints.contains(entrypointId)) continue;

            if (entrypointId == null) {
                // Key was removed from entrypoints — use the captured old index
                data.put(dirtyKey, Optional.empty());
                keys.put(dirtyKey, pendingRemovals.get(dirtyKey));
            } else {
                var value = getByKey(dirtyKey);
                data.put(dirtyKey, Optional.ofNullable(value));
                keys.put(dirtyKey, getIdForKey(dirtyKey));
            }
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
        pendingRemovals.clear();
    }

    /**
     * Returns whether any updates are pending.
     */
    public boolean isDirty() {
        return !pendingUpdates.isEmpty();
    }

    @Override
    public void remove(Key key) {
        if (contains(key)) {
            pendingUpdates.add(key);
            pendingRemovals.put(key, getIdForKey(key));
            super.remove(key);
        }
    }

    @Override
    public <V extends T> V register(Key key, V value, @Nullable NoxesiumEntrypoint entrypoint) {
        var oldValue = getByKey(key);
        var result = super.register(key, value, entrypoint);
        if (!Objects.equals(oldValue, value)) {
            pendingUpdates.add(key);
        }
        // If this key was marked for removal, it's now back — no longer a pending removal
        pendingRemovals.remove(key);
        return result;
    }

    @Override
    public void reset() {
        pendingUpdates.addAll(getKeys());
        pendingRemovals.clear();
        super.reset();
    }
}
