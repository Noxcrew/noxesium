package com.noxcrew.noxesium.paper.api.network.clientbound

import com.noxcrew.noxesium.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets
import it.unimi.dsi.fastutil.ints.IntList

/**
 * Resets the stored value for extra data on an entity.
 */
public class ClientboundResetExtraEntityDataPacket(
    public val entityId: Int,
    public val indices: IntList,
) : NoxesiumPacket(NoxesiumPackets.CLIENT_RESET_EXTRA_ENTITY_DATA)
