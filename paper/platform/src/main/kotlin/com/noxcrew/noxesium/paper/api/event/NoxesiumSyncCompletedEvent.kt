package com.noxcrew.noxesium.paper.api.event

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/** Emitted by Noxesium after it has received a newly updated sync folder from the given client. */
public class NoxesiumSyncCompletedEvent(
    player: Player,
    /** The id of the sync folder that was updated. */
    public val id: String,
) : PlayerEvent(player) {
    public companion object {
        @JvmStatic
        public val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        public fun getHandlerList(): HandlerList = HANDLER_LIST
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST
}
