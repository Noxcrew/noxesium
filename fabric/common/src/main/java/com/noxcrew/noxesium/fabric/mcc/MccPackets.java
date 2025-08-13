package com.noxcrew.noxesium.fabric.mcc;

import static com.noxcrew.noxesium.api.fabric.network.PacketCollection.client;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.api.fabric.network.PacketCollection;

/**
 * Defines all MCC Island integration packets.
 */
public class MccPackets {
    public static final PacketCollection INSTANCE = new PacketCollection();

    public static final NoxesiumPayloadType<ClientboundMccGameStatePacket> CLIENT_MCC_GAME_STATE =
            client(INSTANCE, "clientbound_mcc_game_state", ClientboundMccGameStatePacket.STREAM_CODEC);
    public static final NoxesiumPayloadType<ClientboundMccServerPacket> CLIENT_MCC_SERVER =
            client(INSTANCE, "clientbound_mcc_server", ClientboundMccServerPacket.STREAM_CODEC);
}
