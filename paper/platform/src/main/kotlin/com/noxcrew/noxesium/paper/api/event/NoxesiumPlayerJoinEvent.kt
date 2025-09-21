package com.noxcrew.noxesium.paper.api.event

import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * Emitted by Noxesium when [player] is both a Noxesium player and in the PLAY phase.
 * This can occur either when the player joins after handshaking was completed during
 * the configuration phase, or if handshaking completes late during the play phase.
 */
public class NoxesiumPlayerJoinEvent(
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
