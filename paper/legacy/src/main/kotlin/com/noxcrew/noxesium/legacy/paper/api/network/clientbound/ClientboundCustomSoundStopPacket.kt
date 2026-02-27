package com.noxcrew.noxesium.legacy.paper.api.network.clientbound

import com.noxcrew.noxesium.legacy.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.legacy.paper.api.network.NoxesiumPackets

/**
 * Sent by a server to stop a custom Noxesium sound by its id.
 */
public data class ClientboundCustomSoundStopPacket(
    public val id: Int,
) : NoxesiumPacket(NoxesiumPackets.CLIENT_STOP_SOUND)
