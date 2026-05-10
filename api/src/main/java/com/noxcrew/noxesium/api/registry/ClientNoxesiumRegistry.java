package com.noxcrew.noxesium.api.registry;

import java.util.Optional;
import net.kyori.adventure.key.Key;

/**
 * A variant of the Noxesium registry for the client-side.
 */
public class ClientNoxesiumRegistry<T> extends NoxesiumRegistry<T> {
    public ClientNoxesiumRegistry(Key id) {
        super(id);
    }

    /**
     * Registers a value in this map to the given optional value.
     */
    public void registerAny(Key key, int id, Optional<?> value) {
        register(key, (T) value.get());
        registerMapping(key, id);
    }

    /**
     * Resets only the mappings provided by the server.
     */
    public void resetMappings() {
        idToEntry.clear();
        for (var entry : keyToEntry.values()) {
            entry.setId(-1);
        }
    }

    /**
     * Registers a new mapping from the given key to the given id.
     * Silently fails if the key is not known.
     */
    public boolean registerMapping(Key key, int id) {
        var entry = keyToEntry.get(key);
        if (entry != null) {
            entry.setId(id);
            idToEntry.put(id, entry);
            return true;
        }
        return false;
    }

    @Override
    public void remove(Key key) {
        super.remove(key);
    }
}
