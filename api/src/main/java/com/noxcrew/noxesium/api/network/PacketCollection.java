package com.noxcrew.noxesium.api.network;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadGroup;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

/**
 * A collector of different packets that can be registered and unregistered
 * as a group. Specific to the fabric implementation as it requires pre-registering
 * packet types before packets can be sent. Packet classes themselves are shared.
 */
public final class PacketCollection {
    private final Map<Key, NoxesiumPayloadGroup> packets = new HashMap<>();
    private final Set<String> pluginChannels = new HashSet<>();
    private boolean registered;

    /**
     * Registers a new clientbound packet.
     *
     * @param id  The identifier of this packet.
     * @return The PacketType instance.
     */
    public static NoxesiumPayloadGroup client(PacketCollection collection, String id) {
        return client(collection, NoxesiumReferences.PACKET_NAMESPACE, id);
    }

    /**
     * Registers a new clientbound packet.
     *
     * @param id  The identifier of this packet.
     * @return The PacketType instance.
     */
    public static NoxesiumPayloadGroup client(PacketCollection collection, String namespace, String id) {
        return collection.register(namespace, id, false);
    }

    /**
     * Registers a new serverbound packet.
     *
     * @param id  The identifier of this packet.
     * @param <T> The type of packet.
     * @return The PacketType instance.
     */
    public static NoxesiumPayloadGroup server(PacketCollection collection, String id) {
        return server(collection, NoxesiumReferences.PACKET_NAMESPACE, id);
    }

    /**
     * Registers a new serverbound packet.
     *
     * @param id  The identifier of this packet.
     * @return The PacketType instance.
     */
    public static NoxesiumPayloadGroup server(PacketCollection collection, String namespace, String id) {
        return collection.register(namespace, id, true);
    }

    /**
     * Registers a new packet.
     *
     * @param id  The identifier of this packet.
     * @return The PacketType instance.
     */
    public NoxesiumPayloadGroup register(String namespace, String id, boolean clientToServer) {
        var key = Key.key(namespace, id);
        Preconditions.checkArgument(!packets.containsKey(key));
        var group = new NoxesiumPayloadGroup(this, key, clientToServer);
        packets.put(key, group);
        return group;
    }

    /**
     * Registers a new plugin channel identifier.
     */
    public void addPluginChannelIdentifier(String pluginChannelId) {
        pluginChannels.add(pluginChannelId);
    }

    /**
     * Returns all plugin channel identifiers in this collection.
     */
    public Collection<String> getPluginChannelIdentifiers() {
        return pluginChannels;
    }

    /**
     * Returns the packet groups in this collection.
     */
    public Collection<NoxesiumPayloadGroup> getPackets() {
        return packets.values();
    }

    /**
     * Returns whether this collection has been registered.
     */
    public boolean isRegistered() {
        return registered;
    }

    /**
     * Registers all packets.
     */
    public void register(@Nullable NoxesiumEntrypoint entrypoint) {
        if (registered) return;
        registered = true;
        for (var group : packets.values()) {
            for (var type : group.getPayloadTypes()) {
                type.register(entrypoint);
            }
            NoxesiumNetworking.getInstance().register(group, entrypoint);
        }
    }

    /**
     * Unregisters all packets.
     */
    public void unregister() {
        if (!registered) return;
        registered = false;
        for (var group : packets.values()) {
            NoxesiumNetworking.getInstance().unregister(group);
            for (var type : group.getPayloadTypes()) {
                type.unregister();
            }
        }
    }
}
