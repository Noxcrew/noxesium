package com.noxcrew.noxesium.api.fabric;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.fabric.network.PacketCollection;
import java.util.Collection;
import java.util.List;

/**
 * Extends the {@link NoxesiumEntrypoint} with support for packet collections.
 * Can be defined in `fabric.mod.json` under `noxesium`.
 */
public interface FabricNoxesiumEntrypoint extends NoxesiumEntrypoint {
    /**
     * Returns all packet collections included in this entrypoint.
     */
    default Collection<PacketCollection> getPacketCollections() {
        return List.of();
    }
}
