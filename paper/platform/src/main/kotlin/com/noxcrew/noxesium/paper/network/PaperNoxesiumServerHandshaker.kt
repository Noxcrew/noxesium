package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.core.nms.network.NoxesiumServerHandshaker
import com.noxcrew.noxesium.paper.NoxesiumPaper
import com.noxcrew.noxesium.paper.nms
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRegisterChannelEvent

/**
 * Performs handshaking with the server-side to be run from the client-side.
 */
public class PaperNoxesiumServerHandshaker : NoxesiumServerHandshaker(), Listener {
    override fun register() {
        Bukkit.getPluginManager().registerEvents(this, NoxesiumPaper.plugin)
        super.register()
    }

    @EventHandler
    public fun onPlayerQuit(event: PlayerQuitEvent) {
        onPlayerDisconnect(event.player.uniqueId)
    }

    @EventHandler
    public fun onChannelRegistered(event: PlayerRegisterChannelEvent) {
        onChannelRegistered(event.player.nms, event.channel)
    }

    override fun runDelayed(runnable: Runnable) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(NoxesiumPaper.plugin, runnable)
    }
}
