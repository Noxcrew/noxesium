package com.noxcrew.noxesium.paper.feature

import com.noxcrew.noxesium.core.network.CommonPackets
import com.noxcrew.noxesium.core.network.clientbound.ClientboundStopGlidePacket
import com.noxcrew.noxesium.core.network.serverbound.ServerboundGlidePacket
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.event.CraftEventFactory

/**
 * Sets up the client authoritative elytra.
 */
public class ClientAuthoritativeElytra : ListeningNoxesiumFeature() {
    init {
        // Listen to the client trying to glide
        CommonPackets.SERVER_GLIDE.addListener(this, ServerboundGlidePacket::class.java) { reference, packet, playerId ->
            if (!reference.isRegistered) return@addListener
            val player = Bukkit.getPlayer(playerId) ?: return@addListener

            // Ignore non-client authoritative elytra users from lying about it!
            if (!player.hasNoxesiumComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_ELYTRA)) return@addListener

            // Emit an event, if it gets cancelled we tell off the player. That said, no one should
            // be cancelling this event when using the client elytra as obviously the client will be
            // allowed to elytra for a bit until it gets told off. If the event is cancelled when
            // you stop gliding we don't care, you can't force the player to keep gliding unless you
            // want a constant back and forth.
            val nmsPlayer = (player as CraftPlayer).handle
            val event = CraftEventFactory.callToggleGlideEvent(nmsPlayer, packet.gliding)
            if (event.isCancelled && packet.gliding()) {
                player.noxesiumPlayer?.sendPacket(ClientboundStopGlidePacket.INSTANCE)
                return@addListener
            }

            // Match the player's current state based on what the player says they are doing
            nmsPlayer.setSharedFlag(7, packet.gliding())
        }
    }
}
