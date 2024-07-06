package com.noxcrew.noxesium.paper.api.network.clientbound

import com.noxcrew.noxesium.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets

/** Sent by MCC Island whenever you switch servers. All values are dynamic and may change over time. */
public data class ClientboundMccServerPacket(
    public val serverType: String,
    public val subType: String,
    public val game: String?,
) : NoxesiumPacket(NoxesiumPackets.CLIENT_MCC_SERVER)