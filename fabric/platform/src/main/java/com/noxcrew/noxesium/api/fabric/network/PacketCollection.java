package com.noxcrew.noxesium.api.fabric.network;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.fabric.network.payload.NoxesiumPayloadType;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * A collector of different packets that can be registered and unregistered
 * as a group. Specific to the fabric implementation as it requires pre-registering
 * packet types before packets can be sent. Packet classes themselves are shared.
 */
public final class PacketCollection {
    private final Map<String, NoxesiumPayloadType<?>> packets = new HashMap<>();

    /**
     * Registers a new clientbound packet.
     *
     * @param id  The identifier of this packet.
     * @param <T> The type of packet.
     * @return The PacketType instance.
     */
    public static <T extends NoxesiumPacket> NoxesiumPayloadType<T> client(
            PacketCollection collection, String id, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        return collection.register(id, codec, false);
    }

    /**
     * Registers a new serverbound packet.
     *
     * @param id  The identifier of this packet.
     * @param <T> The type of packet.
     * @return The PacketType instance.
     */
    public static <T extends NoxesiumPacket> NoxesiumPayloadType<T> server(
            PacketCollection collection, String id, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        return collection.register(id, codec, true);
    }

    /**
     * Registers a new packet.
     *
     * @param id  The identifier of this packet.
     * @param <T> The type of packet.
     * @return The PacketType instance.
     */
    public <T extends NoxesiumPacket> NoxesiumPayloadType<T> register(
            String id, StreamCodec<RegistryFriendlyByteBuf, T> codec, boolean clientToServer) {
        Preconditions.checkArgument(!packets.containsKey(id));
        var type = new NoxesiumPayloadType<>(
                ResourceLocation.fromNamespaceAndPath(NoxesiumReferences.PACKET_NAMESPACE, id), codec, clientToServer);
        packets.put(id, type);
        return type;
    }

    /**
     * Registers all packets.
     */
    public void register() {
        for (var type : packets.values()) {
            type.register();
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
