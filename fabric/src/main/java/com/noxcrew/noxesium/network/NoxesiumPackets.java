package com.noxcrew.noxesium.network;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.api.protocol.ProtocolVersion;
import com.noxcrew.noxesium.network.clientbound.ClientboundChangeServerRulesPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundCustomSoundModifyPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundCustomSoundStartPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundCustomSoundStopPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundMccGameStatePacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundMccServerPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundResetExtraEntityDataPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundResetPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundResetServerRulesPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundServerInformationPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundSetExtraEntityDataPacket;
import com.noxcrew.noxesium.network.serverbound.ServerboundClientInformationPacket;
import com.noxcrew.noxesium.network.serverbound.ServerboundClientSettingsPacket;
import com.noxcrew.noxesium.network.serverbound.ServerboundNoxesiumPacket;
import com.noxcrew.noxesium.network.serverbound.ServerboundQibTriggeredPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
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
    public static final String PACKET_NAMESPACE = ProtocolVersion.NAMESPACE + "-v2";

    // Packet types are listed here to ensure they are properly registered!
    public static final NoxesiumPayloadType<ServerboundClientInformationPacket> CLIENT_INFO = NoxesiumPackets.server("client_info", ServerboundClientInformationPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ServerboundClientSettingsPacket> CLIENT_SETTINGS = NoxesiumPackets.server("client_settings", ServerboundClientSettingsPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ServerboundQibTriggeredPacket> QIB_TRIGGERED = NoxesiumPackets.server("qib_triggered", ServerboundQibTriggeredPacket.STREAM_CODEC);

    public static final NoxesiumPayloadType<ClientboundCustomSoundModifyPacket> CUSTOM_SOUND_MODIFY = NoxesiumPackets.client("modify_sound", ClientboundCustomSoundModifyPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundCustomSoundStartPacket> CUSTOM_SOUND_START = NoxesiumPackets.client("start_sound", ClientboundCustomSoundStartPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundCustomSoundStopPacket> CUSTOM_SOUND_STOP = NoxesiumPackets.client("stop_sound", ClientboundCustomSoundStopPacket.STREAM_CODEC);

    public static final NoxesiumPayloadType<ClientboundMccGameStatePacket> MCC_GAME_STATE = NoxesiumPackets.client("mcc_game_state", ClientboundMccGameStatePacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundMccServerPacket> MCC_SERVER = NoxesiumPackets.client("mcc_server", ClientboundMccServerPacket.STREAM_CODEC);

    public static final NoxesiumPayloadType<ClientboundChangeServerRulesPacket> CHANGE_SERVER_RULES = NoxesiumPackets.client("change_server_rules", ClientboundChangeServerRulesPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundResetServerRulesPacket> RESET_SERVER_RULES = NoxesiumPackets.client("reset_server_rules", ClientboundResetServerRulesPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundResetPacket> RESET = NoxesiumPackets.client("reset", ClientboundResetPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundServerInformationPacket> SERVER_INFO = NoxesiumPackets.client("server_info", ClientboundServerInformationPacket.STREAM_CODEC);

    public static final NoxesiumPayloadType<ClientboundSetExtraEntityDataPacket> CHANGE_EXTRA_ENTITY_DATA = NoxesiumPackets.client("change_extra_entity_data", ClientboundSetExtraEntityDataPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundResetExtraEntityDataPacket> RESET_EXTRA_ENTITY_DATA = NoxesiumPackets.client("reset_extra_entity_data", ClientboundResetExtraEntityDataPacket.STREAM_CODEC);

    /**
     * Returns an unmodifiable copy of all registered groups.
     */
    public static Collection<String> getRegisteredGroups() {
        return Collections.unmodifiableCollection(registeredGroups);
    }

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
        var type = new NoxesiumPayloadType<>(new CustomPacketPayload.Type<T>(ResourceLocation.fromNamespaceAndPath(PACKET_NAMESPACE, id)));
        PayloadTypeRegistry.configurationS2C().register(type.type, codec);
        PayloadTypeRegistry.playS2C().register(type.type, codec);
        clientboundPackets.put(id, Pair.of(group, type));

        // If this group has already been registered we also immediately register this packet!
        if (registeredGroups.contains(group)) {
            var universal = Objects.equals(group, "universal");
            if (universal) {
                registerGlobalReceiver(type.type);
            } else {
                registerReceiver(type.type);
            }
        }

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
        var type = new NoxesiumPayloadType<>(new CustomPacketPayload.Type<T>(ResourceLocation.fromNamespaceAndPath(PACKET_NAMESPACE, id)));
        PayloadTypeRegistry.configurationC2S().register(type.type, codec);
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

        // Inform all modules about the new group registration
        NoxesiumMod.getInstance().getAllModules().forEach(it -> it.onGroupRegistered(group));
    }

    /**
     * Unregisters all non-universal packets.
     */
    public static void unregisterPackets() {
        // We only need to clear the local cache as the receivers will disappear
        // along with the play phase addon.
        registeredGroups.removeIf(it -> !Objects.equals(it, "universal"));
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
