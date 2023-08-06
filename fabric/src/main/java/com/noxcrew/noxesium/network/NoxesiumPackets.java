package com.noxcrew.noxesium.network;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.network.clientbound.ClientboundChangeServerRulesPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundMccGameStatePacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundMccServerPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundNoxesiumPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundResetPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundResetServerRulesPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundServerInformationPacket;
import com.noxcrew.noxesium.network.serverbound.ServerboundClientInformationPacket;
import com.noxcrew.noxesium.network.serverbound.ServerboundClientSettingsPacket;
import com.noxcrew.noxesium.network.serverbound.ServerboundNoxesiumPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Defines all different packet types used by Noxesium. Noxesium uses the approach of serializing all incoming plugin channel
 * messages into packets before handling them. While this does come with overhead as the packet implementations cannot be
 * shared between sides it allows code to be more structured and more similar to vanilla's code.
 */
public class NoxesiumPackets {

    private static final Map<String, Pair<String, PacketType<?>>> clientboundPackets = new HashMap<>();
    private static final Map<String, String> serverboundPackets = new HashMap<>();
    private static final Set<String> registeredGroups = new HashSet<>();

    /**
     * The namespace under which all packets are registered. Appended by a global API version equal to the major version of Noxesium.
     */
    public static final String PACKET_NAMESPACE = NoxesiumMod.NAMESPACE + "-v1";

    public static final PacketType<ClientboundChangeServerRulesPacket> CLIENT_CHANGE_SERVER_RULES = client("change_server_rules", ClientboundChangeServerRulesPacket::new);
    public static final PacketType<ClientboundResetServerRulesPacket> CLIENT_RESET_SERVER_RULES = client("reset_server_rules", ClientboundResetServerRulesPacket::new);
    public static final PacketType<ClientboundResetPacket> CLIENT_RESET = client("reset", ClientboundResetPacket::new);
    public static final PacketType<ClientboundServerInformationPacket> CLIENT_SERVER_INFO = client("server_info", ClientboundServerInformationPacket::new);

    public static final PacketType<ClientboundMccServerPacket> CLIENT_MCC_SERVER = client("mcc_server", ClientboundMccServerPacket::new);
    public static final PacketType<ClientboundMccGameStatePacket> CLIENT_MCC_GAME_STATE = client("mcc_game_state", ClientboundMccGameStatePacket::new);

    public static final PacketType<ServerboundClientInformationPacket> SERVER_CLIENT_INFO = server("client_info");
    public static final PacketType<ServerboundClientSettingsPacket> SERVER_CLIENT_SETTINGS = server("client_settings");

    /**
     * Registers a new clientbound Noxesium packet.
     *
     * @param id          The identifier of this packet.
     * @param constructor A constructor that creates this packet when given a byte buffer.
     * @param <T>         The type of packet.
     * @return The PacketType instance.
     */
    public static <T extends ClientboundNoxesiumPacket> PacketType<T> client(String id, Function<FriendlyByteBuf, T> constructor) {
        return client(id, "universal", constructor);
    }

    /**
     * Registers a new clientbound Noxesium packet.
     *
     * @param id          The identifier of this packet.
     * @param group       The group this packet belongs to, this can be used to selectively register packets based on the server being used.
     * @param constructor A constructor that creates this packet when given a byte buffer.
     * @param <T>         The type of packet.
     * @return The PacketType instance.
     */
    public static <T extends ClientboundNoxesiumPacket> PacketType<T> client(String id, String group, Function<FriendlyByteBuf, T> constructor) {
        Preconditions.checkArgument(!clientboundPackets.containsKey(id));
        Preconditions.checkArgument(!serverboundPackets.containsKey(id));
        var type = PacketType.create(new ResourceLocation(PACKET_NAMESPACE, id), constructor);
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
    public static <T extends ServerboundNoxesiumPacket> PacketType<T> server(String id) {
        return server(id, "universal");
    }

    /**
     * Registers a new serverbound Noxesium packet.
     *
     * @param id    The identifier of this packet.
     * @param group The group this packet belongs to, this can be used to selectively register packets based on the server being used.
     * @param <T>   The type of packet.
     * @return The PacketType instance.
     */
    public static <T extends ServerboundNoxesiumPacket> PacketType<T> server(String id, String group) {
        Preconditions.checkArgument(!clientboundPackets.containsKey(id));
        Preconditions.checkArgument(!serverboundPackets.containsKey(id));
        var type = PacketType.<T>create(new ResourceLocation(PACKET_NAMESPACE, id), (buffer) -> {
            throw new UnsupportedOperationException("Serverbound Noxesium packets cannot be de-serialized!");
        });
        serverboundPackets.put(type.getId().toString(), group);
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

            var type = (PacketType<FabricPacket>) packet.getSecond();
            var handler = new NoxesiumPacketHandler();
            if (universal) {
                ClientPlayNetworking.registerGlobalReceiver(type, handler);
            } else {
                ClientPlayNetworking.registerReceiver(type, handler);
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
    public static boolean canSend(PacketType<?> type) {
        var group = serverboundPackets.get(type.getId().toString());
        Preconditions.checkNotNull(group, "Could not find the packet type " + type.getId().toString());
        return registeredGroups.contains(group);
    }
}
