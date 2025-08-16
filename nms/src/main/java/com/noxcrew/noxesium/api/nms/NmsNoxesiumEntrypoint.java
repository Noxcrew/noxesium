package com.noxcrew.noxesium.api.nms;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.nms.network.PacketCollection;
import java.util.Collection;
import java.util.List;

/**
 * Extends the {@link NoxesiumEntrypoint} with support for packet collections.
 */
public interface NmsNoxesiumEntrypoint extends NoxesiumEntrypoint {
    /**
     * Returns all packet collections included in this entrypoint.
     */
    default Collection<PacketCollection> getPacketCollections() {
        return List.of();
    }
}
