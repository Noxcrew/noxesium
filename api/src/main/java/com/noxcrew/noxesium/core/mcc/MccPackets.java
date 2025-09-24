package com.noxcrew.noxesium.core.mcc;

import static com.noxcrew.noxesium.api.network.PacketCollection.client;

import com.noxcrew.noxesium.api.network.PacketCollection;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadGroup;

/**
 * Defines all MCC Island integration packets.
 */
public class MccPackets {
    public static final PacketCollection INSTANCE = new PacketCollection();

    public static final NoxesiumPayloadGroup CLIENTBOUND_MCC_GAME_STATE =
            client(INSTANCE, "clientbound_mcc_game_state").markLazy().add(ClientboundMccGameStatePacket.class);
    public static final NoxesiumPayloadGroup CLIENTBOUND_MCC_SERVER =
            client(INSTANCE, "clientbound_mcc_server").markLazy().add(ClientboundMccServerPacket.class);
    public static final NoxesiumPayloadGroup CLIENTBOUND_MCC_STATISTIC =
            client(INSTANCE, "clientbound_mcc_statistic").markLazy().add(ClientboundMccStatisticPacket.class);
}
