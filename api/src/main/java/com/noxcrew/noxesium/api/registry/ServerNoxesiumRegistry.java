package com.noxcrew.noxesium.api.registry;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

/**
 * A variant of the Noxesium registry for the server-side.
 */
public class ServerNoxesiumRegistry<T> extends NoxesiumRegistry<T> {
    protected final AtomicInteger lastId = new AtomicInteger();
    protected final Map<Key, String> entrypoints = new HashMap<>();

    public ServerNoxesiumRegistry(Key id) {
        super(id);
    }

    /**
     * Returns all ids of this registry to sync with clients for the given entrypoints.
     */
    public Map<Key, Integer> determineAllSyncableIds(List<String> entrypoints) {
        var result = new HashMap<Key, Integer>();
        for (var entry : this.entrypoints.entrySet()) {
            // Ignore keys for entrypoints this player does not have know about!
            if (entry.getValue() != null && !entrypoints.contains(entry.getValue())) continue;

            // Determine the id of this key
            var value = getByKey(entry.getKey());
            var index = getIdFor(value);
            result.put(entry.getKey(), index);
        }
        return result;
    }

    @Override
    public void reset() {
        super.reset();
        lastId.set(0);
        entrypoints.clear();
    }

    @Override
    public <V extends T> V register(Key key, V value, @Nullable NoxesiumEntrypoint entrypoint) {
        super.register(key, value, entrypoint);

        // Don't re-register if the value is already included!
        if (byId.containsValue(value)) return value;

        var id = lastId.getAndIncrement();
        byId.put(id, value);
        entrypoints.put(key, entrypoint == null ? null : entrypoint.getId());
        return value;
    }

    @Override
    public void remove(Key key) {
        var value = byKey.get(key);
        if (value == null) return;
        super.remove(key);
        entrypoints.remove(key);

        var id = byId.inverse().get(value);
        byId.remove(id);
    }
}
