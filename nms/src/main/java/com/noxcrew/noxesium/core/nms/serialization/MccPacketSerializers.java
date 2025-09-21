package com.noxcrew.noxesium.core.nms.serialization;

import static com.noxcrew.noxesium.api.nms.serialization.PacketSerializerRegistry.registerPlaySerializer;

import com.noxcrew.noxesium.core.mcc.ClientboundMccGameStatePacket;
import com.noxcrew.noxesium.core.mcc.ClientboundMccServerPacket;
import com.noxcrew.noxesium.core.mcc.ClientboundMccStatisticPacket;
import com.noxcrew.noxesium.core.mcc.MccPackets;
import java.util.ArrayList;
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
        registerPlaySerializer(
                MccPackets.CLIENTBOUND_MCC_GAME_STATE,
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundMccGameStatePacket::game,
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundMccGameStatePacket::queueType,
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
        registerPlaySerializer(
                MccPackets.CLIENTBOUND_MCC_SERVER,
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundMccServerPacket::server,
                        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
                        ClientboundMccServerPacket::types,
                        ClientboundMccServerPacket::new));
        registerPlaySerializer(
                MccPackets.CLIENTBOUND_MCC_STATISTIC,
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundMccStatisticPacket::statistic,
                        ByteBufCodecs.BOOL,
                        ClientboundMccStatisticPacket::record,
                        ByteBufCodecs.VAR_INT,
                        ClientboundMccStatisticPacket::value,
                        ClientboundMccStatisticPacket::new));
    }
}
