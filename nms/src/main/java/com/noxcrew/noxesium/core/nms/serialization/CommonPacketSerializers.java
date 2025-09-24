package com.noxcrew.noxesium.core.nms.serialization;

import static com.noxcrew.noxesium.api.nms.serialization.PacketSerializerRegistry.registerSerializer;

import com.noxcrew.noxesium.api.nms.NoxesiumPlatform;
import com.noxcrew.noxesium.api.nms.codec.NoxesiumCodecs;
import com.noxcrew.noxesium.api.nms.codec.NoxesiumStreamCodecs;
import com.noxcrew.noxesium.api.nms.serialization.SerializableRegistries;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundModifyPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundStartPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundStopPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundOpenLinkPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundOpenLinkV2Packet;
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
 * Defines all common Noxesium packet serializers.
 */
public class CommonPacketSerializers {
    /**
     * Registers all serializers.
     */
    public static void register() {
        SerializableRegistries.registerSerializers(
                NoxesiumRegistries.QIB_EFFECTS, NoxesiumCodecs.QIB_DEFINITION, NoxesiumStreamCodecs.QIB_DEFINITION);

        registerSerializer(
                ServerboundClientSettingsPacket.class,
                StreamCodec.composite(
                        NoxesiumStreamCodecs.CLIENT_SETTINGS,
                        ServerboundClientSettingsPacket::settings,
                        ServerboundClientSettingsPacket::new));
        registerSerializer(
                ServerboundQibTriggeredPacket.class,
                StreamCodec.composite(
                        NoxesiumStreamCodecs.KEY,
                        ServerboundQibTriggeredPacket::behavior,
                        NoxesiumStreamCodecs.forEnum(ServerboundQibTriggeredPacket.Type.class),
                        ServerboundQibTriggeredPacket::qibType,
                        ByteBufCodecs.VAR_INT,
                        ServerboundQibTriggeredPacket::entityId,
                        ServerboundQibTriggeredPacket::new));
        registerSerializer(
                ServerboundRiptidePacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, ServerboundRiptidePacket::slot, ServerboundRiptidePacket::new));
        registerSerializer(
                ServerboundMouseButtonClickPacket.class,
                StreamCodec.composite(
                        NoxesiumStreamCodecs.forEnum(ServerboundMouseButtonClickPacket.Action.class),
                        ServerboundMouseButtonClickPacket::action,
                        NoxesiumStreamCodecs.forEnum(ServerboundMouseButtonClickPacket.Button.class),
                        ServerboundMouseButtonClickPacket::button,
                        ServerboundMouseButtonClickPacket::new));
        registerSerializer(
                ClientboundCustomSoundModifyPacket.class,
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
        registerSerializer(
                ClientboundCustomSoundStartPacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        ClientboundCustomSoundStartPacket::id,
                        NoxesiumStreamCodecs.KEY,
                        ClientboundCustomSoundStartPacket::sound,
                        NoxesiumStreamCodecs.forEnum(Sound.Source.class),
                        ClientboundCustomSoundStartPacket::source,
                        ByteBufCodecs.FLOAT,
                        ClientboundCustomSoundStartPacket::volume,
                        ByteBufCodecs.FLOAT,
                        ClientboundCustomSoundStartPacket::pitch,
                        ByteBufCodecs.FLOAT,
                        ClientboundCustomSoundStartPacket::offset,
                        ByteBufCodecs.BOOL,
                        ClientboundCustomSoundStartPacket::looping,
                        ByteBufCodecs.BOOL,
                        ClientboundCustomSoundStartPacket::attenuation,
                        ByteBufCodecs.BOOL,
                        ClientboundCustomSoundStartPacket::ignoreIfPlaying,
                        ByteBufCodecs.optional(ByteBufCodecs.VECTOR3F),
                        ClientboundCustomSoundStartPacket::position,
                        ByteBufCodecs.optional(ByteBufCodecs.VAR_INT),
                        ClientboundCustomSoundStartPacket::entityId,
                        ClientboundCustomSoundStartPacket::new));
        registerSerializer(
                ClientboundCustomSoundStopPacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        ClientboundCustomSoundStopPacket::id,
                        ClientboundCustomSoundStopPacket::new));
        registerSerializer(
                ClientboundUpdateEntityComponentsPacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        ClientboundUpdateEntityComponentsPacket::entityId,
                        ByteBufCodecs.BOOL,
                        ClientboundUpdateEntityComponentsPacket::reset,
                        NoxesiumStreamCodecs.noxesiumComponentPatch(NoxesiumRegistries.ENTITY_COMPONENTS),
                        ClientboundUpdateEntityComponentsPacket::patch,
                        ClientboundUpdateEntityComponentsPacket::new));
        registerSerializer(
                ClientboundUpdateGameComponentsPacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.BOOL,
                        ClientboundUpdateGameComponentsPacket::reset,
                        NoxesiumStreamCodecs.noxesiumComponentPatch(NoxesiumRegistries.GAME_COMPONENTS),
                        ClientboundUpdateGameComponentsPacket::patch,
                        ClientboundUpdateGameComponentsPacket::new));
        registerSerializer(
                ClientboundOpenLinkPacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.optional(NoxesiumPlatform.getInstance().getComponentStreamCodec()),
                        ClientboundOpenLinkPacket::text,
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundOpenLinkPacket::url,
                        ClientboundOpenLinkPacket::new));
        registerSerializer(
                ClientboundOpenLinkV2Packet.class,
                StreamCodec.composite(
                        ByteBufCodecs.optional(NoxesiumPlatform.getInstance().getComponentStreamCodec()),
                        ClientboundOpenLinkV2Packet::text,
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundOpenLinkV2Packet::url,
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundOpenLinkV2Packet::test,
                        ClientboundOpenLinkV2Packet::new));
    }
}
