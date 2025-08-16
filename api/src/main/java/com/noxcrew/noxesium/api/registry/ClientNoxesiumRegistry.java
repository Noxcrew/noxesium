package com.noxcrew.noxesium.api.registry;

import net.kyori.adventure.key.Key;

/**
 * A variant of the Noxesium registry for the client-side.
 */
public class ClientNoxesiumRegistry<T> extends NoxesiumRegistry<T> {
    /**
     * Registers a new mapping from the given key to the given id.
     * Silently fails if the key is not known.
     */
    public void registerMapping(int id, Key key) {
        var value = getByKey(key);
        if (value != null) {
            byId.put(id, value);
            forId.put(value, id);
        }
    }
}
