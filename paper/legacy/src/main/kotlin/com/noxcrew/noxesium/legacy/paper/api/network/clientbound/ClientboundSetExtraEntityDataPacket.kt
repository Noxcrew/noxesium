package com.noxcrew.noxesium.legacy.paper.api.network.clientbound

import com.noxcrew.noxesium.legacy.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.legacy.paper.api.network.NoxesiumPackets
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Changes the value of extra entity data on a target entity.
 */
public data class ClientboundSetExtraEntityDataPacket(
    public val entityId: Int,
    public val writers: Map<Int, (RegistryFriendlyByteBuf) -> Unit>,
) : NoxesiumPacket(NoxesiumPackets.CLIENT_CHANGE_EXTRA_ENTITY_DATA)
