package com.noxcrew.noxesium.paper.api.network.serverbound

import com.noxcrew.noxesium.api.protocol.NoxesiumFeature
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets
import net.minecraft.network.RegistryFriendlyByteBuf
import org.bukkit.entity.Player

/**
 * Sent to the server when the client first joins to establish the version of the
 * client being used.
 */
public data class ServerboundClientInformationPacket(
    public val protocolVersion: Int,
    public val versionString: String,
) : ServerboundNoxesiumPacket(NoxesiumPackets.SERVER_CLIENT_INFO) {

    public constructor(buffer: RegistryFriendlyByteBuf, player: Player, protocolVersion: Int) : this(
        buffer,
        buffer.readVarInt(),
    )

    public constructor(buffer: RegistryFriendlyByteBuf, protocolVersion: Int) : this(
        protocolVersion,
        if (protocolVersion >= NoxesiumFeature.NEW_MCC_FEATURES.minProtocolVersion) buffer.readUtf() else "unknown"
    )
}
