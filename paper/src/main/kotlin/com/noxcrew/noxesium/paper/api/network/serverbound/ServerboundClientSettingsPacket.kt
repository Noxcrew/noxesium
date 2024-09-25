package com.noxcrew.noxesium.paper.api.network.serverbound

import com.noxcrew.noxesium.api.protocol.ClientSettings
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets
import net.minecraft.network.RegistryFriendlyByteBuf
import org.bukkit.entity.Player

/**
 * Sent to the server to inform it about various settings configured by the client,
 * mostly geared towards the size of their GUI and other visual-related settings.
 */
public data class ServerboundClientSettingsPacket(
    public val settings: ClientSettings,
) : ServerboundNoxesiumPacket(NoxesiumPackets.SERVER_CLIENT_SETTINGS) {

    public constructor(
        buffer: RegistryFriendlyByteBuf,
        player: Player,
        protocolVersion: Int,
    ) : this(
        ClientSettings(
            buffer.readVarInt(),
            buffer.readDouble(),
            buffer.readVarInt(),
            buffer.readVarInt(),
            buffer.readBoolean(),
            buffer.readBoolean(),
            buffer.readDouble(),
        )
    )
}
