package com.noxcrew.noxesium.core.nms.serialization;

import static com.noxcrew.noxesium.api.nms.serialization.PacketSerializerRegistry.registerSerializer;

import com.noxcrew.noxesium.core.mcc.ClientboundMccGameStatePacket;
import com.noxcrew.noxesium.core.mcc.ClientboundMccServerPacket;
import com.noxcrew.noxesium.core.mcc.MccPackets;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Defines all MCC Island integration packet serializers.
 */
public class MccPacketSerializers {
    /**
     * Registers all serializers.
     */
    public static void register() {
        registerSerializer(
                MccPackets.CLIENTBOUND_MCC_GAME_STATE,
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundMccGameStatePacket::phaseType,
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundMccGameStatePacket::stage,
                        ByteBufCodecs.VAR_INT,
                        ClientboundMccGameStatePacket::round,
                        ByteBufCodecs.VAR_INT,
                        ClientboundMccGameStatePacket::totalRounds,
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundMccGameStatePacket::mapId,
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundMccGameStatePacket::mapName,
                        ClientboundMccGameStatePacket::new));
        registerSerializer(
                MccPackets.CLIENTBOUND_MCC_SERVER,
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundMccServerPacket::serverType,
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundMccServerPacket::subType,
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundMccServerPacket::associatedGame,
                        ClientboundMccServerPacket::new));
    }
}
