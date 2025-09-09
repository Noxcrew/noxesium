package com.noxcrew.noxesium.legacy.api.network

/**
 * The basis for a server-side packet as used by Noxesium.
 */
public abstract class NoxesiumPacket(
    /** The packet type of this packet. */
    public open val type: PacketType<*>,
)
