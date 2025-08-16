package com.noxcrew.noxesium.core.mcc;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;

/**
 * Sent by MCC Island whenever you switch servers. All values are dynamic and may change over time.
 */
public record ClientboundMccServerPacket(String serverType, String subType, String associatedGame)
        implements NoxesiumPacket {}
