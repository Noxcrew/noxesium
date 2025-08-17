package com.noxcrew.noxesium.api.registry;

import net.kyori.adventure.key.Key;

/**
 * A variant of the Noxesium registry for the client-side.
 */
public class ClientNoxesiumRegistry<T> extends NoxesiumRegistry<T> {
    public ClientNoxesiumRegistry(Key id) {
        super(id);
    }

    /**
     * Registers a new mapping from the given key to the given id.
     * Silently fails if the key is not known.
     */
    public boolean registerMapping(int id, Key key) {
        var value = getByKey(key);
        if (value != null) {
            byId.put(id, value);
            return true;
        }
        return false;
    }
}
