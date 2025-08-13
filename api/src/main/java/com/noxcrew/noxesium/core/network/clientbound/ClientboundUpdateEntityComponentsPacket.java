package com.noxcrew.noxesium.core.network.clientbound;

import com.noxcrew.noxesium.api.component.NoxesiumComponentPatch;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;

/**
 * Changes the values of entity components on the client.
 */
public record ClientboundUpdateEntityComponentsPacket(int entityId, boolean reset, NoxesiumComponentPatch patch)
        implements NoxesiumPacket {}
