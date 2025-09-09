package com.noxcrew.noxesium.legacy.paper.api.network.clientbound

import com.noxcrew.noxesium.legacy.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.legacy.paper.api.network.NoxesiumPackets
import net.kyori.adventure.text.Component

/**
 * Sent by the server to open a link dialog on the client.
 */
public data class ClientboundOpenLinkPacket(
    public val text: Component?,
    public val url: String,
) : NoxesiumPacket(NoxesiumPackets.CLIENT_OPEN_LINK)
