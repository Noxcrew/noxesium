package com.noxcrew.noxesium.paper.api.network.clientbound

import com.noxcrew.noxesium.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets

/**
 * Sent to the client when the server is first informed of it existing, this contains information
 * about what protocol version the server supports.
 */
public data class ClientboundServerInformationPacket(
    public val maxProtocolVersion: Int,
) : NoxesiumPacket(NoxesiumPackets.CLIENT_SERVER_INFO)
