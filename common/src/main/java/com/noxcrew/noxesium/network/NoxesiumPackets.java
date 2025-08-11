package com.noxcrew.noxesium.network;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.network.clientbound.ClientboundChangeServerRulesPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundCustomSoundModifyPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundCustomSoundStartPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundCustomSoundStopPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundMccGameStatePacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundMccServerPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundOpenLinkPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundResetExtraEntityDataPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundResetPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundResetServerRulesPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundServerInformationPacket;
import com.noxcrew.noxesium.network.clientbound.ClientboundSetExtraEntityDataPacket;
import com.noxcrew.noxesium.network.serverbound.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

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
    public static final String PACKET_NAMESPACE = NoxesiumReferences.NAMESPACE + "-v2";

    // Packet types are listed here to ensure they are properly registered!
    public static final NoxesiumPayloadType<ServerboundClientInformationPacket> SERVER_CLIENT_INFO =
            NoxesiumPackets.server("client_info", ServerboundClientInformationPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ServerboundClientSettingsPacket> SERVER_CLIENT_SETTINGS =
            NoxesiumPackets.server("client_settings", ServerboundClientSettingsPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ServerboundQibTriggeredPacket> SERVER_QIB_TRIGGERED =
            NoxesiumPackets.server("qib_triggered", ServerboundQibTriggeredPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ServerboundRiptidePacket> SERVER_RIPTIDE =
            NoxesiumPackets.server("riptide", ServerboundRiptidePacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ServerboundMouseButtonClickPacket> SERVER_MOUSE_BUTTON_CLICK =
            NoxesiumPackets.server("mouse_button_click", ServerboundMouseButtonClickPacket.STREAM_CODEC);

    public static final NoxesiumPayloadType<ClientboundCustomSoundModifyPacket> CLIENT_CUSTOM_SOUND_MODIFY =
            NoxesiumPackets.client("modify_sound", ClientboundCustomSoundModifyPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundCustomSoundStartPacket> CLIENT_CUSTOM_SOUND_START =
            NoxesiumPackets.client("start_sound", ClientboundCustomSoundStartPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundCustomSoundStopPacket> CLIENT_CUSTOM_SOUND_STOP =
            NoxesiumPackets.client("stop_sound", ClientboundCustomSoundStopPacket.STREAM_CODEC);

    public static final NoxesiumPayloadType<ClientboundMccGameStatePacket> CLIENT_MCC_GAME_STATE =
            NoxesiumPackets.client("mcc_game_state", ClientboundMccGameStatePacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundMccServerPacket> CLIENT_MCC_SERVER =
            NoxesiumPackets.client("mcc_server", ClientboundMccServerPacket.STREAM_CODEC);

    public static final NoxesiumPayloadType<ClientboundChangeServerRulesPacket> CLIENT_CHANGE_SERVER_RULES =
            NoxesiumPackets.client("change_server_rules", ClientboundChangeServerRulesPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundResetServerRulesPacket> CLIENT_RESET_SERVER_RULES =
            NoxesiumPackets.client("reset_server_rules", ClientboundResetServerRulesPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundResetPacket> CLIENT_RESET =
            NoxesiumPackets.client("reset", ClientboundResetPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundServerInformationPacket> CLIENT_SERVER_INFO =
            NoxesiumPackets.client("server_info", ClientboundServerInformationPacket.STREAM_CODEC);

    public static final NoxesiumPayloadType<ClientboundSetExtraEntityDataPacket> CLIENT_CHANGE_EXTRA_ENTITY_DATA =
            NoxesiumPackets.client("change_extra_entity_data", ClientboundSetExtraEntityDataPacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundResetExtraEntityDataPacket> CLIENT_RESET_EXTRA_ENTITY_DATA =
            NoxesiumPackets.client("reset_extra_entity_data", ClientboundResetExtraEntityDataPacket.STREAM_CODEC);

    public static final NoxesiumPayloadType<ClientboundOpenLinkPacket> CLIENT_OPEN_LINK =
            NoxesiumPackets.client("open_link", ClientboundOpenLinkPacket.STREAM_CODEC);

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
    public static <T extends NoxesiumPacket> NoxesiumPayloadType<T> client(
            String id, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
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
    public static <T extends NoxesiumPacket> NoxesiumPayloadType<T> client(
            String id, String group, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        Preconditions.checkArgument(!clientboundPackets.containsKey(id));
        Preconditions.checkArgument(!serverboundPackets.containsKey(id));
        var type = new NoxesiumPayloadType<>(
                new CustomPacketPayload.Type<T>(ResourceLocation.fromNamespaceAndPath(PACKET_NAMESPACE, id)));
        NoxesiumMod.getPlatform().registerPacket(type, codec, false);
        clientboundPackets.put(id, Pair.of(group, type));

        // If this group has already been registered we also immediately register this packet!
        if (registeredGroups.contains(group)) {
            var universal = Objects.equals(group, "universal");
            NoxesiumMod.getPlatform().registerReceiver(type.type, universal);
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
    public static <T extends ServerboundNoxesiumPacket> NoxesiumPayloadType<T> server(
            String id, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
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
    public static <T extends ServerboundNoxesiumPacket> NoxesiumPayloadType<T> server(
            String id, String group, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        Preconditions.checkArgument(!clientboundPackets.containsKey(id));
        Preconditions.checkArgument(!serverboundPackets.containsKey(id));
        var type = new NoxesiumPayloadType<>(
                new CustomPacketPayload.Type<T>(ResourceLocation.fromNamespaceAndPath(PACKET_NAMESPACE, id)));
        NoxesiumMod.getPlatform().registerPacket(type, codec, true);
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
        Preconditions.checkArgument(
                !registeredGroups.contains(group), "Cannot double register packets for group " + group);

        var universal = Objects.equals(group, "universal");
        for (var packet : clientboundPackets.values()) {
            if (!Objects.equals(group, packet.getFirst())) continue;

            var type = packet.getSecond();
            NoxesiumMod.getPlatform().registerReceiver(type.type, universal);
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
        if (!NoxesiumMod.getPlatform().canSend(type)) return false;

        var group = serverboundPackets.get(type.id().toString());
        Preconditions.checkNotNull(
                group, "Could not find the packet type " + type.id().toString());
        return registeredGroups.contains(group);
    }
}
