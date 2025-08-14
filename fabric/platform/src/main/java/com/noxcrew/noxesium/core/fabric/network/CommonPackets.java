package com.noxcrew.noxesium.core.fabric.network;

import static com.noxcrew.noxesium.api.fabric.network.PacketCollection.client;
import static com.noxcrew.noxesium.api.fabric.network.PacketCollection.server;

import com.noxcrew.noxesium.api.fabric.network.PacketCollection;
import com.noxcrew.noxesium.api.fabric.network.payload.NoxesiumPayloadType;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumStreamCodecs;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundModifyPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundStartPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundStopPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundOpenLinkPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundUpdateEntityComponentsPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundUpdateGameComponentsPacket;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundClientSettingsPacket;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundMouseButtonClickPacket;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundQibTriggeredPacket;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundRiptidePacket;
import net.kyori.adventure.sound.Sound;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Defines all common Noxesium packets.
 */
public class CommonPackets {
    public static final PacketCollection INSTANCE = new PacketCollection();

    public static final NoxesiumPayloadType<ServerboundClientSettingsPacket> SERVER_CLIENT_SETTINGS = server(
            INSTANCE,
            "serverbound_client_settings",
            StreamCodec.composite(
                    NoxesiumStreamCodecs.CLIENT_SETTINGS,
                    ServerboundClientSettingsPacket::settings,
                    ServerboundClientSettingsPacket::new));

    public static final NoxesiumPayloadType<ServerboundQibTriggeredPacket> SERVER_QIB_TRIGGERED = server(
            INSTANCE,
            "serverbound_qib_triggered",
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    ServerboundQibTriggeredPacket::behavior,
                    NoxesiumStreamCodecs.forEnum(ServerboundQibTriggeredPacket.Type.class),
                    ServerboundQibTriggeredPacket::qibType,
                    ByteBufCodecs.VAR_INT,
                    ServerboundQibTriggeredPacket::entityId,
                    ServerboundQibTriggeredPacket::new));

    public static final NoxesiumPayloadType<ServerboundRiptidePacket> SERVER_RIPTIDE = server(
            INSTANCE,
            "serverbound_riptide",
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ServerboundRiptidePacket::slot, ServerboundRiptidePacket::new));

    public static final NoxesiumPayloadType<ServerboundMouseButtonClickPacket> SERVER_MOUSE_BUTTON_CLICK = server(
            INSTANCE,
            "serverbound_mouse_button_click",
            StreamCodec.composite(
                    NoxesiumStreamCodecs.forEnum(ServerboundMouseButtonClickPacket.Action.class),
                    ServerboundMouseButtonClickPacket::action,
                    NoxesiumStreamCodecs.forEnum(ServerboundMouseButtonClickPacket.Button.class),
                    ServerboundMouseButtonClickPacket::button,
                    ServerboundMouseButtonClickPacket::new));

    public static final NoxesiumPayloadType<ClientboundCustomSoundModifyPacket> CLIENT_CUSTOM_SOUND_MODIFY = client(
            INSTANCE,
            "clientbound_modify_sound",
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ClientboundCustomSoundModifyPacket::id,
                    ByteBufCodecs.FLOAT,
                    ClientboundCustomSoundModifyPacket::volume,
                    ByteBufCodecs.VAR_INT,
                    ClientboundCustomSoundModifyPacket::interpolationTicks,
                    ByteBufCodecs.optional(ByteBufCodecs.FLOAT),
                    ClientboundCustomSoundModifyPacket::startVolume,
                    ClientboundCustomSoundModifyPacket::new));

    public static final NoxesiumPayloadType<ClientboundCustomSoundStartPacket> CLIENT_CUSTOM_SOUND_START = client(
            INSTANCE,
            "clientbound_start_sound",
            NoxesiumStreamCodecs.composite(
                    ByteBufCodecs.VAR_INT,
                    ClientboundCustomSoundStartPacket::id,
                    NoxesiumStreamCodecs.KEY,
                    ClientboundCustomSoundStartPacket::sound,
                    NoxesiumStreamCodecs.forEnum(Sound.Source.class),
                    ClientboundCustomSoundStartPacket::source,
                    ByteBufCodecs.BOOL,
                    ClientboundCustomSoundStartPacket::looping,
                    ByteBufCodecs.BOOL,
                    ClientboundCustomSoundStartPacket::attenuation,
                    ByteBufCodecs.BOOL,
                    ClientboundCustomSoundStartPacket::ignoreIfPlaying,
                    ByteBufCodecs.FLOAT,
                    ClientboundCustomSoundStartPacket::volume,
                    ByteBufCodecs.FLOAT,
                    ClientboundCustomSoundStartPacket::pitch,
                    ByteBufCodecs.optional(ByteBufCodecs.VECTOR3F),
                    ClientboundCustomSoundStartPacket::position,
                    ByteBufCodecs.optional(ByteBufCodecs.VAR_INT),
                    ClientboundCustomSoundStartPacket::entityId,
                    ByteBufCodecs.optional(ByteBufCodecs.LONG),
                    ClientboundCustomSoundStartPacket::unix,
                    ByteBufCodecs.optional(ByteBufCodecs.VAR_INT),
                    ClientboundCustomSoundStartPacket::offset,
                    ClientboundCustomSoundStartPacket::new));

    public static final NoxesiumPayloadType<ClientboundCustomSoundStopPacket> CLIENT_CUSTOM_SOUND_STOP = client(
            INSTANCE,
            "clientbound_stop_sound",
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ClientboundCustomSoundStopPacket::id,
                    ClientboundCustomSoundStopPacket::new));

    public static final NoxesiumPayloadType<ClientboundUpdateEntityComponentsPacket> CLIENT_UPDATE_ENTITY_COMPONENTS =
            client(
                    INSTANCE,
                    "clientbound_update_entity_components",
                    StreamCodec.composite(
                            ByteBufCodecs.VAR_INT,
                            ClientboundUpdateEntityComponentsPacket::entityId,
                            ByteBufCodecs.BOOL,
                            ClientboundUpdateEntityComponentsPacket::reset,
                            NoxesiumStreamCodecs.noxesiumComponentPatch(NoxesiumRegistries.ENTITY_COMPONENTS),
                            ClientboundUpdateEntityComponentsPacket::patch,
                            ClientboundUpdateEntityComponentsPacket::new));

    public static final NoxesiumPayloadType<ClientboundUpdateGameComponentsPacket> CLIENT_UPDATE_GAME_COMPONENTS =
            client(
                    INSTANCE,
                    "clientbound_update_game_components",
                    StreamCodec.composite(
                            ByteBufCodecs.BOOL,
                            ClientboundUpdateGameComponentsPacket::reset,
                            NoxesiumStreamCodecs.noxesiumComponentPatch(NoxesiumRegistries.GAME_COMPONENTS),
                            ClientboundUpdateGameComponentsPacket::patch,
                            ClientboundUpdateGameComponentsPacket::new));

    public static final NoxesiumPayloadType<ClientboundOpenLinkPacket> CLIENT_OPEN_LINK = client(
            INSTANCE,
            "clientbound_open_link",
            StreamCodec.composite(
                    ByteBufCodecs.optional(NoxesiumStreamCodecs.COMPONENT),
                    ClientboundOpenLinkPacket::text,
                    ByteBufCodecs.STRING_UTF8,
                    ClientboundOpenLinkPacket::url,
                    ClientboundOpenLinkPacket::new));
}
