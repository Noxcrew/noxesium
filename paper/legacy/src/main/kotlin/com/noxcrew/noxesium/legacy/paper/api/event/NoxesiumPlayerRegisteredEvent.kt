package com.noxcrew.noxesium.legacy.paper.api.event

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/** Emitted by [NoxesiumManager] when it registers a new player. */
public class NoxesiumPlayerRegisteredEvent(
    player: Player,
    /** The new protocol version of the player. */
    public val protocolVersion: Int,
    /** The raw version string of the player's installed Noxesium jar. */
    public val version: String,
) : PlayerEvent(player) {
    public companion object {
        @JvmStatic
        public val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        public fun getHandlerList(): HandlerList = HANDLER_LIST
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST
}
