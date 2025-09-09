package com.noxcrew.noxesium.legacy.api.network.clientbound

import com.noxcrew.noxesium.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets

/**
 * Sent by a server to stop a custom Noxesium sound by its id.
 */
public data class ClientboundCustomSoundStopPacket(
    public val id: Int,
) : NoxesiumPacket(NoxesiumPackets.CLIENT_STOP_SOUND)
