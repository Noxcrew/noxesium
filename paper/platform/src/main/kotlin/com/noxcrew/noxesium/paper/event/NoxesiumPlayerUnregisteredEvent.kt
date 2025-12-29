package com.noxcrew.noxesium.paper.event

import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/** Emitted by Noxesium when it unregisters a player. */
public class NoxesiumPlayerUnregisteredEvent(
    /** The Noxesium player being unregistered. */
    public val noxesiumPlayer: NoxesiumServerPlayer,
) : Event() {
    public companion object {
        @JvmStatic
        public val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        public fun getHandlerList(): HandlerList = HANDLER_LIST
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST
}
