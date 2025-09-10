package com.noxcrew.noxesium.paper.api.event

import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/** Emitted by Noxesium when it unregisters a player. */
public class NoxesiumPlayerUnregisteredEvent(
    player: Player,
    /** The Noxesium player being unregistered. */
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
