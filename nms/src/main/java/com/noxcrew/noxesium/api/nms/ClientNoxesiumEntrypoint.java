package com.noxcrew.noxesium.api.nms;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;

/**
 * Extends the {@link NoxesiumEntrypoint} with client-sided methods.
 * Can be defined in `fabric.mod.json` under `noxesium`.
 */
public interface ClientNoxesiumEntrypoint extends NmsNoxesiumEntrypoint {
    /**
     * Returns the protocol version of this entrypoint.
     */
    int getProtocolVersion();

    /**
     * Returns the version of this entrypoint.
     */
    default String getRawVersion() {
        return Integer.toString(getProtocolVersion());
    }
}
