package com.noxcrew.noxesium.core.fabric.mcc;

import com.noxcrew.noxesium.api.nms.ClientNoxesiumEntrypoint;
import com.noxcrew.noxesium.api.nms.network.PacketCollection;
import com.noxcrew.noxesium.core.nms.mcc.MccPackets;
import java.util.Collection;
import java.util.List;

/**
 * Implements MCC specific Noxesium packets.
 */
public class MccNoxesiumEntrypoint implements ClientNoxesiumEntrypoint {

    @Override
    public String getId() {
        return "noxesium-mcc";
    }

    @Override
    public int getProtocolVersion() {
        return 1;
    }

    @Override
    public Collection<PacketCollection> getPacketCollections() {
        return List.of(MccPackets.INSTANCE);
    }
}
