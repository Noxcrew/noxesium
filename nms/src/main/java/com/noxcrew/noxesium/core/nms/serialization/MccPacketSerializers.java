package com.noxcrew.noxesium.core.nms.serialization;

import static com.noxcrew.noxesium.api.nms.serialization.PacketSerializerRegistry.registerSerializer;

import com.noxcrew.noxesium.core.mcc.ClientboundMccGameStatePacket;
import com.noxcrew.noxesium.core.mcc.ClientboundMccServerPacket;
import com.noxcrew.noxesium.core.mcc.ClientboundMccStatisticPacket;
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
        registerSerializer(
                ClientboundMccGameStatePacket.class,
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
        registerSerializer(
                ClientboundMccServerPacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundMccServerPacket::server,
                        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
                        ClientboundMccServerPacket::types,
                        ClientboundMccServerPacket::new));
        registerSerializer(
                ClientboundMccStatisticPacket.class,
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
