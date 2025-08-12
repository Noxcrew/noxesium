package com.noxcrew.noxesium.api.registry;

import net.kyori.adventure.key.Key;

import java.util.concurrent.atomic.AtomicInteger;

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
    public void register(Key key, T value) {
        super.register(key, value);

        var id = lastId.getAndIncrement();
        byId.put(id, value);
    }
}
