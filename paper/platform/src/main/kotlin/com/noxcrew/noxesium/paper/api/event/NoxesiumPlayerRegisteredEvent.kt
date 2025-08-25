package com.noxcrew.noxesium.paper.api.event

import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/** Emitted by [NoxesiumManager] when it finishes a handshake with a new player. */
public class NoxesiumPlayerRegisteredEvent(
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
