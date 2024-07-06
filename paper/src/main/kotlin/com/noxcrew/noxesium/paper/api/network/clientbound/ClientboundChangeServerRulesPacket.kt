package com.noxcrew.noxesium.paper.api.network.clientbound

import com.noxcrew.noxesium.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets
import net.minecraft.network.FriendlyByteBuf

/**
 * Changes the stored value for one or more server rules.
 */
public data class ClientboundChangeServerRulesPacket(
    public val writers: Map<Int, (FriendlyByteBuf) -> Unit>,
) : NoxesiumPacket(NoxesiumPackets.CLIENT_CHANGE_SERVER_RULES)
