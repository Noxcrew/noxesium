package com.noxcrew.noxesium.core.mcc;

import static com.noxcrew.noxesium.api.network.PacketCollection.client;

import com.noxcrew.noxesium.api.network.PacketCollection;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType;

/**
 * Defines all MCC Island integration packets.
 */
public class MccPackets {
    public static final PacketCollection INSTANCE = new PacketCollection();

    public static final NoxesiumPayloadType<ClientboundMccGameStatePacket> CLIENTBOUND_MCC_GAME_STATE =
            client(INSTANCE, "clientbound_mcc_game_state", ClientboundMccGameStatePacket.class);
    public static final NoxesiumPayloadType<ClientboundMccServerPacket> CLIENTBOUND_MCC_SERVER =
            client(INSTANCE, "clientbound_mcc_server", ClientboundMccServerPacket.class);
    public static final NoxesiumPayloadType<ClientboundMccStatisticPacket> CLIENTBOUND_MCC_STATISTIC =
            client(INSTANCE, "clientbound_mcc_statistic", ClientboundMccStatisticPacket.class);
}
