package com.noxcrew.noxesium.legacy.paper.api.network

/** A type of packet. */
public open class PacketType<T : NoxesiumPacket>(
    /** The id of this packet. */
    public val id: String,
)
