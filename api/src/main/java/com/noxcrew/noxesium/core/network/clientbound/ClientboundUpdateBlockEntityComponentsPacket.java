package com.noxcrew.noxesium.core.network.clientbound;

import com.noxcrew.noxesium.api.component.NoxesiumComponentPatch;
import com.noxcrew.noxesium.api.network.NoxesiumRegistryDependentPacket;
import org.joml.Vector3i;

/**
 * Changes the values of block entity components on the client.
 */
public record ClientboundUpdateBlockEntityComponentsPacket(
        Vector3i blockPos, boolean reset, NoxesiumComponentPatch patch) implements NoxesiumRegistryDependentPacket {}
