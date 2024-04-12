package com.noxcrew.noxesium.network;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.network.payload.NoxesiumPayloadType;
import com.noxcrew.noxesium.network.serverbound.ServerboundNoxesiumPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Defines all different packet types used by Noxesium. Noxesium uses the approach of serializing all incoming plugin channel
 * messages into packets before handling them. While this does come with overhead as the packet implementations cannot be
 * shared between sides it allows code to be more structured and more similar to vanilla's code.
 */
public class NoxesiumPackets {

    private static final Map<String, Pair<String, NoxesiumPayloadType<?>>> clientboundPackets = new HashMap<>();
    private static final Map<String, String> serverboundPackets = new HashMap<>();
    private static final Set<String> registeredGroups = new HashSet<>();

    /**
     * The namespace under which all packets are registered. Appended by a global API version equal to the major version of Noxesium.
     */
    public static final String PACKET_NAMESPACE = NoxesiumMod.NAMESPACE + "-v2";

    /**
     * Registers a new clientbound Noxesium packet.
     *
     * @param id  The identifier of this packet.
     * @param <T> The type of packet.
     * @return The PacketType instance.
     */
    public static <T extends NoxesiumPacket> NoxesiumPayloadType<T> client(String id, StreamCodec<FriendlyByteBuf, T> codec) {
        return client(id, "universal", codec);
    }

    /**
     * Registers a new clientbound Noxesium packet.
     *
     * @param id    The identifier of this packet.
     * @param group The group this packet belongs to, this can be used to selectively register packets based on the server being used.
     * @param <T>   The type of packet.
     * @return The PacketType instance.
     */
    public static <T extends NoxesiumPacket> NoxesiumPayloadType<T> client(String id, String group, StreamCodec<FriendlyByteBuf, T> codec) {
        Preconditions.checkArgument(!clientboundPackets.containsKey(id));
        Preconditions.checkArgument(!serverboundPackets.containsKey(id));
        var type = new NoxesiumPayloadType<>(new CustomPacketPayload.Type<T>(new ResourceLocation(PACKET_NAMESPACE, id)));
        PayloadTypeRegistry.playS2C().register(type.type, codec);
        clientboundPackets.put(id, Pair.of(group, type));
        return type;
    }

    /**
     * Registers a new serverbound Noxesium packet.
     *
     * @param id  The identifier of this packet.
     * @param <T> The type of packet.
     * @return The PacketType instance.
     */
    public static <T extends ServerboundNoxesiumPacket> NoxesiumPayloadType<T> server(String id, StreamCodec<FriendlyByteBuf, T> codec) {
        return server(id, "universal", codec);
    }

    /**
     * Registers a new serverbound Noxesium packet.
     *
     * @param id    The identifier of this packet.
     * @param group The group this packet belongs to, this can be used to selectively register packets based on the server being used.
     * @param <T>   The type of packet.
     * @return The PacketType instance.
     */
    public static <T extends ServerboundNoxesiumPacket> NoxesiumPayloadType<T> server(String id, String group, StreamCodec<FriendlyByteBuf, T> codec) {
        Preconditions.checkArgument(!clientboundPackets.containsKey(id));
        Preconditions.checkArgument(!serverboundPackets.containsKey(id));
        var type = new NoxesiumPayloadType<>(new CustomPacketPayload.Type<T>(new ResourceLocation(PACKET_NAMESPACE, id)));
        PayloadTypeRegistry.playC2S().register(type.type, codec);
        serverboundPackets.put(type.id().toString(), group);
        return type;
    }

    /**
     * Registers all packets of the given group. The grouping system exists for other
     * mods to hook into and register some non-universal server-specific channel. This
     * channel can be given its own conditions for becoming active.
     *
     * @param group The group to register all packets for.
     */
    public static void registerPackets(String group) {
        Preconditions.checkArgument(!registeredGroups.contains(group), "Cannot double register packets for group " + group);

        var universal = Objects.equals(group, "universal");
        for (var packet : clientboundPackets.values()) {
            if (!Objects.equals(group, packet.getFirst())) continue;

            var type = packet.getSecond();
            if (universal) {
                registerGlobalReceiver(type.type);
            } else {
                registerReceiver(type.type);
            }
        }

        // We don't need to register server-bound packets, we only store that the
        // group was enabled.
        registeredGroups.add(group);
    }

    /**
     * Checks if the connected server should be receiving packets of the given type.
     *
     * @param type The packet type
     * @return Whether the connected server should be receiving the packet
     */
    public static boolean canSend(NoxesiumPayloadType<?> type) {
        var group = serverboundPackets.get(type.id().toString());
        Preconditions.checkNotNull(group, "Could not find the packet type " + type.id().toString());
        return registeredGroups.contains(group);
    }

    /**
     * Registers a new regular receiver.
     */
    private static <T extends CustomPacketPayload> void registerReceiver(CustomPacketPayload.Type<T> type) {
        var handler = new NoxesiumPacketHandler<T>();
        ClientPlayNetworking.registerReceiver(type, handler);
    }

    /**
     * Registers a new global receiver.
     */
    private static <T extends CustomPacketPayload> void registerGlobalReceiver(CustomPacketPayload.Type<T> type) {
        var handler = new NoxesiumPacketHandler<T>();
        ClientPlayNetworking.registerGlobalReceiver(type, handler);
    }
}
