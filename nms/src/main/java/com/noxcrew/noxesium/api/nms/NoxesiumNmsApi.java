package com.noxcrew.noxesium.api.nms;

import com.noxcrew.noxesium.api.nms.network.PacketCollection;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides NMS-platform specific APIs.
 */
public class NoxesiumNmsApi {
    private static NoxesiumNmsApi instance;

    private final Set<PacketCollection> packets = new HashSet<>();

    /**
     * Returns the singleton instance of the Noxesium NMS API.
     */
    public static NoxesiumNmsApi getInstance() {
        if (instance == null) {
            instance = new NoxesiumNmsApi();
        }
        return instance;
    }

    /**
     * Registers a new collection of packets.
     * Usually called by the initializer.
     */
    public void registerPackets(PacketCollection collection) {
        packets.add(collection);
        collection.register();
    }

    /**
     * Unregisters all features and packet collections.
     */
    public void unregisterAll() {
        packets.forEach(PacketCollection::unregister);
        packets.clear();
        NoxesiumRegistries.REGISTRIES.forEach(NoxesiumRegistry::reset);
    }
}
