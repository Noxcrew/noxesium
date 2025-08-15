package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.network.clientbound.ClientboundHandshakeAcknowledgePacket
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakePacket
import com.noxcrew.noxesium.api.nms.network.HandshakePackets
import com.noxcrew.noxesium.api.nms.network.NoxesiumClientboundNetworking
import com.noxcrew.noxesium.paper.NoxesiumPaper
import net.minecraft.world.entity.player.Player
import org.bukkit.Bukkit
import org.bukkit.event.Listener

/**
 * Performs handshaking for Noxesium.
 */
public class NoxesiumServerHandshaker : Listener {
    /** Registers the handshaker. */
    public fun register() {
        // Listen to events for plugin channels being registered which indicates readiness
        Bukkit.getPluginManager().registerEvents(this, NoxesiumPaper.plugin)

        // TODO handle server rules and client settings in some other handler file!

        // Respond to incoming handshake packets
        HandshakePackets.SERVERBOUND_HANDSHAKE.addListener(this) { _, packet, player ->
            handleHandshake(player, packet)
        }

        // Register the handshaking packets so clients know they can authenticate here!
        HandshakePackets.INSTANCE.register()
    }

    /** Handles a client initiating a handshake with the server. */
    private fun handleHandshake(player: Player, packet: ServerboundHandshakePacket) {
        // Accept all incoming entry points and respond to them
        val entrypoints = mutableMapOf<String, String>()
        println("Received $packet from ${player.gameProfile.name}")
        NoxesiumClientboundNetworking.send(player, ClientboundHandshakeAcknowledgePacket(entrypoints))
    }

    /*@EventHandler
    public fun onChannelRegistered(event: PlayerRegisterChannelEvent) {
        if (event.channel != Key.key(PACKET_NAMESPACE, NoxesiumPackets.CLIENT_SERVER_INFO.id).asString()) return

        // Delay by a tick so the other channels can get registered. We assume all channels
        // are registered in one batch!
        val player = event.player
        Bukkit.getScheduler().scheduleSyncDelayedTask(
            NoxesiumPaper.plugin,
            {
                if (!player.isConnected) return@scheduleSyncDelayedTask
                ready += player.uniqueId
                onReady(player)
                val (protocolVersion, version) = pending.remove(player.uniqueId) ?: return@scheduleSyncDelayedTask
                registerPlayer(player, protocolVersion, version)
            },
        )
    }*/
}