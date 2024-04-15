package com.noxcrew.noxesium.paper.api.network.serverbound

import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets
import net.minecraft.network.FriendlyByteBuf

/**
 * Sent to the server when the client first joins to establish the version of the
 * client being used.
 */
public class ServerboundClientInformationPacket(
    public val protocolVersion: Int,
) : ServerboundNoxesiumPacket(NoxesiumPackets.SERVER_CLIENT_INFO) {

    public constructor(buffer: FriendlyByteBuf) : this(buffer.readVarInt())
}
