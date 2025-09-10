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
        byId.clear();
    }

    /**
     * Registers a new mapping from the given key to the given id.
     * Silently fails if the key is not known.
     */
    public boolean registerMapping(Key key, int id) {
        var value = getByKey(key);
        if (value != null) {
            byId.put(id, value);
            return true;
        }
        return false;
    }

    @Override
    public void remove(Key key) {
        var value = byKey.get(key);
        if (value == null) return;
        super.remove(key);

        var id = byId.inverse().get(value);
        byId.remove(id);
    }
}
