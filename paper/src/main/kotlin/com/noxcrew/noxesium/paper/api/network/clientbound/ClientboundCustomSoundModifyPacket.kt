package com.noxcrew.noxesium.paper.api.network.clientbound

import com.noxcrew.noxesium.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets

/**
 * Sent by a server to change the volume of a sound. The interpolation time can be
 * used to fade the sound up or down over an amount of ticks.
 */
public class ClientboundCustomSoundModifyPacket(
    public val id: Int,
    public val volume: Float,
    public val interpolationTicks: Int,
    /** An optional volume to start the interpolation from. If absent the current volume of the sound is used instead. */
    public val startVolume: Float? = null,
) : NoxesiumPacket(NoxesiumPackets.CLIENT_MODIFY_SOUND)
