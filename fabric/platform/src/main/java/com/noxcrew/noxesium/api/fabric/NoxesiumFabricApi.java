package com.noxcrew.noxesium.api.fabric;

import com.noxcrew.noxesium.api.fabric.network.PacketCollection;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides Fabric-platform specific APIs.
 */
public class NoxesiumFabricApi {
    private static NoxesiumFabricApi instance;

    private final Set<PacketCollection> packets = new HashSet<>();

    /**
     * Returns the singleton instance of the Noxesium Fabric API.
     */
    public static NoxesiumFabricApi getInstance() {
        if (instance == null) {
            instance = new NoxesiumFabricApi();
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
