package com.noxcrew.noxesium.api.registry;

import java.util.concurrent.atomic.AtomicInteger;
import net.kyori.adventure.key.Key;

/**
 * A variant of the Noxesium registry for the server-side.
 */
public class ServerNoxesiumRegistry<T> extends NoxesiumRegistry<T> {
    private final AtomicInteger lastId = new AtomicInteger();

    @Override
    public void reset() {
        super.reset();
        lastId.set(0);
    }

    @Override
    public <V extends T> V register(Key key, V value) {
        super.register(key, value);

        var id = lastId.getAndIncrement();
        byId.put(id, value);
        return value;
    }
}
