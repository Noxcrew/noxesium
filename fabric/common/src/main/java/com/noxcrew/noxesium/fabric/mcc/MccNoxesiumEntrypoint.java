package com.noxcrew.noxesium.fabric.mcc;

import com.noxcrew.noxesium.api.fabric.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.fabric.network.PacketCollection;
import java.util.Collection;
import java.util.List;

/**
 * Implements MCC specific Noxesium packets.
 */
public class MccNoxesiumEntrypoint implements NoxesiumEntrypoint {

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
