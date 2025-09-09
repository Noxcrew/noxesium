package com.noxcrew.noxesium.core.network.clientbound;

import com.noxcrew.noxesium.api.component.NoxesiumComponentPatch;
import com.noxcrew.noxesium.api.network.NoxesiumRegistryDependentPacket;

/**
 * Changes the values of game components on the client.
 */
public record ClientboundUpdateGameComponentsPacket(boolean reset, NoxesiumComponentPatch patch)
        implements NoxesiumRegistryDependentPacket {}
