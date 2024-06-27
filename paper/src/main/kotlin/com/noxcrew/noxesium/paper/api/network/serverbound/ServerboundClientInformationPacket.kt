package com.noxcrew.noxesium.paper.api.network.serverbound

import com.noxcrew.noxesium.api.protocol.NoxesiumFeature
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets
import net.minecraft.network.FriendlyByteBuf
import org.bukkit.entity.Player

/**
 * Sent to the server when the client first joins to establish the version of the
 * client being used.
 */
public class ServerboundClientInformationPacket(
    public val protocolVersion: Int,
    public val versionString: String,
) : ServerboundNoxesiumPacket(NoxesiumPackets.SERVER_CLIENT_INFO) {

    public constructor(buffer: FriendlyByteBuf, player: Player, protocolVersion: Int) : this(
        buffer.readVarInt(),
        if (protocolVersion >= NoxesiumFeature.NEW_MCC_FEATURES.minProtocolVersion) buffer.readUtf() else "unknown"
    )
}
