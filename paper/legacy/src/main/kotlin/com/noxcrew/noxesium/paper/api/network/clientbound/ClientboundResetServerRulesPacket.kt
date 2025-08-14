package com.noxcrew.noxesium.paper.api.network.clientbound

import com.noxcrew.noxesium.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets
import it.unimi.dsi.fastutil.ints.IntList

/**
 * Resets the stored value for one or more server rules.
 */
public data class ClientboundResetServerRulesPacket(
    public val indices: IntList,
) : NoxesiumPacket(NoxesiumPackets.CLIENT_RESET_SERVER_RULES)
