package com.noxcrew.noxesium.fabric.mcc;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.api.fabric.network.PacketCollection;

/**
 * Defines all MCC Island integration packets.
 */
public class MccPackets extends PacketCollection {
    /**
     * The instance of the MCC Island packet collection.
     */
    public static final MccPackets INSTANCE = new MccPackets();

    public final NoxesiumPayloadType<ClientboundMccGameStatePacket> CLIENT_MCC_GAME_STATE =
            client("clientbound_mcc_game_state", ClientboundMccGameStatePacket.STREAM_CODEC);
    public final NoxesiumPayloadType<ClientboundMccServerPacket> CLIENT_MCC_SERVER =
            client("clientbound_mcc_server", ClientboundMccServerPacket.STREAM_CODEC);
}
