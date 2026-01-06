package com.noxcrew.noxesium.paper.feature.game

import com.noxcrew.noxesium.core.network.CommonPackets
import com.noxcrew.noxesium.core.network.clientbound.ClientboundGlidePacket
import com.noxcrew.noxesium.core.network.serverbound.ServerboundGlidePacket
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes
import com.noxcrew.noxesium.paper.component.hasNoxesiumComponent
import com.noxcrew.noxesium.paper.component.noxesiumPlayer
import com.noxcrew.noxesium.paper.feature.ListeningNoxesiumFeature
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.event.CraftEventFactory
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityToggleGlideEvent

/**
 * Sets up the client authoritative elytra.
 */
public class ClientAuthoritativeElytra : ListeningNoxesiumFeature() {
    private var ignoreEvent: Boolean = false

    init {
        // Listen to the client trying to glide
        CommonPackets.SERVER_GLIDE.addListener(this, ServerboundGlidePacket::class.java) { reference, packet, playerId ->
            if (!reference.isRegistered) return@addListener
            val player = Bukkit.getPlayer(playerId) ?: return@addListener

            // Ignore non-client authoritative elytra users from lying about it!
            if (!player.hasNoxesiumComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_ELYTRA)) return@addListener

            // Emit an event, if it gets cancelled we tell off the player. That said, no one should
            // be cancelling this event when using the client elytra as obviously the client will be
            // allowed to elytra for a bit until it gets told off.
            val nmsPlayer = (player as CraftPlayer).handle
            reference.ignoreEvent = true
            val event = CraftEventFactory.callToggleGlideEvent(nmsPlayer, packet.gliding)
            reference.ignoreEvent = false
            if (event.isCancelled) {
                player.noxesiumPlayer?.sendPacket(ClientboundGlidePacket(!packet.gliding()))
                return@addListener
            }

            // Match the player's current state based on what the player says they are doing
            if (packet.gliding()) {
                nmsPlayer.setSharedFlag(7, true)
            } else {
                nmsPlayer.setSharedFlag(7, true)
                nmsPlayer.setSharedFlag(7, false)
            }
        }
    }

    /** Prevents gliding from being toggled outside of this class. */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public fun onToggleGlide(e: EntityToggleGlideEvent) {
        // Ignore this event for regular players
        val player = e.entity as? Player ?: return
        if (!player.hasNoxesiumComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_ELYTRA)) return

        // Ignore if we're running this for the custom elytra
        if (ignoreEvent) return

        // Prevent Bukkit from saving any changes to the gliding state!
        e.isCancelled = true
    }
}
