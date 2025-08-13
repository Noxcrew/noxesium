package com.noxcrew.noxesium.core.fabric.mcc;

import com.noxcrew.noxesium.api.fabric.FabricNoxesiumEntrypoint;
import com.noxcrew.noxesium.api.fabric.network.PacketCollection;
import java.util.Collection;
import java.util.List;

/**
 * Implements MCC specific Noxesium packets.
 */
public class MccNoxesiumEntrypoint implements FabricNoxesiumEntrypoint {

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
