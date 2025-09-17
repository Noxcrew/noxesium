package com.noxcrew.noxesium.core.fabric.mcc;

import com.noxcrew.noxesium.api.ClientNoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.PacketCollection;
import com.noxcrew.noxesium.core.mcc.MccPackets;
import com.noxcrew.noxesium.core.nms.serialization.MccPacketSerializers;
import java.util.Collection;
import java.util.List;

/**
 * Implements MCC specific Noxesium packets.
 */
public class MccNoxesiumEntrypoint implements ClientNoxesiumEntrypoint {

    public MccNoxesiumEntrypoint() {
        MccPacketSerializers.register();
    }

    @Override
    public String getVersion() {
        return "2.0";
    }

    @Override
    public String getId() {
        return "noxesium-mcc";
    }

    @Override
    public Collection<PacketCollection> getPacketCollections() {
        return List.of(MccPackets.INSTANCE);
    }
}
