package com.noxcrew.noxesium.paper.api.network.clientbound

import com.noxcrew.noxesium.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets

/**
 * Sent by the server to reset one or more features of the client.
 * The flags byte has the following results:
 * 0x01 - Resets all server rule values
 * 0x02 - Resets cached player heads
 */
public class ClientboundResetPacket(
    public val flags: Byte,
) : NoxesiumPacket(NoxesiumPackets.CLIENT_RESET)
