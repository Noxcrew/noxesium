package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.NoxesiumApi
import com.noxcrew.noxesium.api.network.handshake.HandshakePackets
import com.noxcrew.noxesium.api.network.handshake.NoxesiumServerHandshaker
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import com.noxcrew.noxesium.paper.NoxesiumPaper
import com.noxcrew.noxesium.paper.api.event.NoxesiumPlayerRegisteredEvent
import com.noxcrew.noxesium.paper.feature.noxesiumPlayer
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRegisterChannelEvent

/**
 * Performs handshaking with the server-side to be run from the client-side.
 */
public class PaperNoxesiumServerHandshaker : NoxesiumServerHandshaker(), Listener {
    override fun register() {
        super.register()
        Bukkit.getPluginManager().registerEvents(this, NoxesiumPaper.plugin)

        // Respond to initial handshake packet and create the server player instance
        HandshakePackets.SERVERBOUND_HANDSHAKE.addListener(
            this,
        ) { reference, packet, playerId ->
            if (NoxesiumPlayerManager.getInstance().getPlayer(playerId) != null) {
                NoxesiumApi.getLogger().error("Received registry contents while player was known, destroying connection!")
                destroy(playerId)
                return@addListener
            }

            val bukkitPlayer = Bukkit.getPlayer(playerId) as CraftPlayer
            val serverPlayer = bukkitPlayer.handle
            reference.handleHandshake(PaperServerPlayer(serverPlayer), packet!!)
        }
    }

    @EventHandler
    public fun onPlayerQuit(event: PlayerQuitEvent) {
        onPlayerDisconnect(event.player.uniqueId)
    }

    @EventHandler
    public fun onChannelRegistered(event: PlayerRegisterChannelEvent) {
        onChannelRegistered(event.player.noxesiumPlayer, event.channel)
    }

    override fun isConnected(player: NoxesiumServerPlayer): Boolean =
        super.isConnected(player) && !(player as PaperServerPlayer).player.hasDisconnected()

    override fun runDelayed(runnable: Runnable) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(NoxesiumPaper.plugin, runnable)
    }

    override fun completeHandshake(player: NoxesiumServerPlayer): Boolean = if (super.completeHandshake(player)) {
        // Emit an event for other systems to hook into
        Bukkit
            .getPluginManager()
            .callEvent(NoxesiumPlayerRegisteredEvent((player as PaperServerPlayer).player.bukkitEntity, player))
        true
    } else {
        false
    }
}
