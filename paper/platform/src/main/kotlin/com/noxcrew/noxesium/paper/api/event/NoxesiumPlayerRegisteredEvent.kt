package com.noxcrew.noxesium.paper.api.event

import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/** Emitted by Noxesium when it finishes a handshake with a new player. */
public class NoxesiumPlayerRegisteredEvent(
    /** The Noxesium player which stores information provided by this client. */
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
