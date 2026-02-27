package com.noxcrew.noxesium.legacy.paper.api.network.serverbound

import com.noxcrew.noxesium.legacy.paper.api.network.NoxesiumPackets
import net.minecraft.network.RegistryFriendlyByteBuf
import org.bukkit.entity.Player

/**
 * Sent to the server to inform it that the client just triggered a qib interaction.
 */
public data class ServerboundQibTriggeredPacket(
    public val behavior: String,
    public val qibType: Type,
    public val entityId: Int,
) : ServerboundNoxesiumPacket(NoxesiumPackets.SERVER_QIB_TRIGGERED) {
    /**
     * The type of qib interaction the client triggered.
     */
    public enum class Type {
        JUMP,
        INSIDE,
        ENTER,
        LEAVE,
    }

    public constructor(buffer: RegistryFriendlyByteBuf, player: Player, protocolVersion: Int) : this(
        buffer.readUtf(),
        buffer.readEnum(Type::class.java),
        buffer.readVarInt(),
    )
}
