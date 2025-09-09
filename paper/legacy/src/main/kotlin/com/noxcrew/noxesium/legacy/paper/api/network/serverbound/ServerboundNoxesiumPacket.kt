package com.noxcrew.noxesium.legacy.paper.api.network.serverbound

import com.noxcrew.noxesium.legacy.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.legacy.paper.api.network.ServerboundPacketType
import org.bukkit.entity.Player

/**
 * A packet received from Noxesium clients.
 */
public abstract class ServerboundNoxesiumPacket(
    override val type: ServerboundPacketType<*>,
) : NoxesiumPacket(type)

/** Handles a new packet from [player]. */
public fun <T : ServerboundNoxesiumPacket> T.handle(player: Player) {
    (type as ServerboundPacketType<T>).handle(player, this)
}
