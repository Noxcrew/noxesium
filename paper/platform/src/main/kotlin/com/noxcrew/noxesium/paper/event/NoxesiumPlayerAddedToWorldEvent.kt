package com.noxcrew.noxesium.paper.event

import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * Emitted by Noxesium when [player] is a Noxesium player and added to the world, this
 * can be after a handshake finished while in the world or if the player was already
 * handshook and just entered the world.
 */
public class NoxesiumPlayerAddedToWorldEvent(
    player: Player,
    /** The Noxesium player which stores information provided by this client. */
    public val noxesiumPlayer: NoxesiumServerPlayer,
) : PlayerEvent(player) {
    public companion object {
        @JvmStatic
        public val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        public fun getHandlerList(): HandlerList = HANDLER_LIST
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST
}
