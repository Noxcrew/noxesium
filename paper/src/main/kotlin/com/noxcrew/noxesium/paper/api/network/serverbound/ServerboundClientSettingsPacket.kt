package com.noxcrew.noxesium.paper.api.network.serverbound

import com.noxcrew.noxesium.api.protocol.ClientSettings
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets
import com.noxcrew.noxesium.paper.api.network.serverbound.ServerboundNoxesiumPacket
import net.minecraft.network.FriendlyByteBuf

/**
 * Sent to the server to inform it about various settings configured by the client,
 * mostly geared towards the size of their GUI and other visual-related settings.
 */
public class ServerboundClientSettingsPacket(
    public val settings: ClientSettings,
) : ServerboundNoxesiumPacket(NoxesiumPackets.SERVER_CLIENT_SETTINGS) {

    public constructor(
        buffer: FriendlyByteBuf,
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
