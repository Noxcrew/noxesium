package com.noxcrew.noxesium.core.nms.mcc;

import static com.noxcrew.noxesium.api.nms.network.PacketCollection.client;

import com.noxcrew.noxesium.api.nms.network.PacketCollection;
import com.noxcrew.noxesium.api.nms.network.payload.NoxesiumPayloadType;
import com.noxcrew.noxesium.core.mcc.ClientboundMccGameStatePacket;
import com.noxcrew.noxesium.core.mcc.ClientboundMccServerPacket;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Defines all MCC Island integration packets.
 */
public class MccPackets {
    public static final PacketCollection INSTANCE = new PacketCollection();

    public static final NoxesiumPayloadType<ClientboundMccGameStatePacket> CLIENTBOUND_MCC_GAME_STATE = client(
            INSTANCE,
            "clientbound_mcc_game_state",
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

    public static final NoxesiumPayloadType<ClientboundMccServerPacket> CLIENTBOUND_MCC_SERVER = client(
            INSTANCE,
            "clientbound_mcc_server",
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    ClientboundMccServerPacket::serverType,
                    ByteBufCodecs.STRING_UTF8,
                    ClientboundMccServerPacket::subType,
                    ByteBufCodecs.STRING_UTF8,
                    ClientboundMccServerPacket::associatedGame,
                    ClientboundMccServerPacket::new));
}
