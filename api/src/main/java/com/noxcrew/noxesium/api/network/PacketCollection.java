package com.noxcrew.noxesium.api.network;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

/**
 * A collector of different packets that can be registered and unregistered
 * as a group. Specific to the fabric implementation as it requires pre-registering
 * packet types before packets can be sent. Packet classes themselves are shared.
 */
public final class PacketCollection {
    private final Map<String, NoxesiumPayloadType<?>> packets = new HashMap<>();
    private final Set<String> pluginChannels = new HashSet<>();
    private final boolean configPhaseCompatible;

    /**
     * Registers a new clientbound packet.
     *
     * @param id  The identifier of this packet.
     * @param <T> The type of packet.
     * @return The PacketType instance.
     */
    public static <T extends NoxesiumPacket> NoxesiumPayloadType<T> client(
            PacketCollection collection, String id, Class<T> clazz) {
        return client(collection, NoxesiumReferences.PACKET_NAMESPACE, id, clazz);
    }

    /**
     * Registers a new clientbound packet.
     *
     * @param id  The identifier of this packet.
     * @param <T> The type of packet.
     * @return The PacketType instance.
     */
    public static <T extends NoxesiumPacket> NoxesiumPayloadType<T> client(
            PacketCollection collection, String namespace, String id, Class<T> clazz) {
        return collection.register(namespace, id, clazz, false);
    }

    /**
     * Registers a new serverbound packet.
     *
     * @param id  The identifier of this packet.
     * @param <T> The type of packet.
     * @return The PacketType instance.
     */
    public static <T extends NoxesiumPacket> NoxesiumPayloadType<T> server(
            PacketCollection collection, String id, Class<T> clazz) {
        return server(collection, NoxesiumReferences.PACKET_NAMESPACE, id, clazz);
    }

    /**
     * Registers a new serverbound packet.
     *
     * @param id  The identifier of this packet.
     * @param <T> The type of packet.
     * @return The PacketType instance.
     */
    public static <T extends NoxesiumPacket> NoxesiumPayloadType<T> server(
            PacketCollection collection, String namespace, String id, Class<T> clazz) {
        return collection.register(namespace, id, clazz, true);
    }

    /**
     * Registers a new packet.
     *
     * @param id  The identifier of this packet.
     * @param <T> The type of packet.
     * @return The PacketType instance.
     */
    public <T extends NoxesiumPacket> NoxesiumPayloadType<T> register(
            String namespace, String id, Class<T> clazz, boolean clientToServer) {
        Preconditions.checkArgument(!packets.containsKey(id));
        var type = NoxesiumNetworking.getInstance()
                .createPayloadType(namespace, id, clazz, clientToServer, configPhaseCompatible);
        packets.put(id, type);
        pluginChannels.add(type.id().asString());
        return type;
    }

    public PacketCollection() {
        this(false);
    }

    public PacketCollection(boolean configPhaseCompatible) {
        this.configPhaseCompatible = configPhaseCompatible;
    }

    /**
     * Returns all plugin channel identifiers in this collection.
     */
    public Collection<String> getPluginChannelIdentifiers() {
        return pluginChannels;
    }

    /**
     * Returns the packet types in this collection.
     */
    public Collection<NoxesiumPayloadType<?>> getPackets() {
        return packets.values();
    }

    /**
     * Registers all packets.
     */
    public void register(@Nullable NoxesiumEntrypoint entrypoint) {
        for (var type : packets.values()) {
            type.register(entrypoint);
        }
    }

    /**
     * Unregisters all packets.
     */
    public void unregister() {
        for (var type : packets.values()) {
            type.unregister();
        }
    }
}
