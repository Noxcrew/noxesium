package com.noxcrew.noxesium.api.nms;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.nms.network.PacketCollection;
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
    public void registerPackets(NoxesiumEntrypoint entrypoint, PacketCollection collection) {
        packets.add(collection);
        collection.register(entrypoint);
    }

    /**
     * Unregisters all features and packet collections.
     */
    public void unregisterAll() {
        packets.forEach(PacketCollection::unregister);
        packets.clear();
    }
}
