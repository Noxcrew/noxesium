package com.noxcrew.packet

import io.netty.channel.ChannelPipeline
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.slf4j.LoggerFactory

/** Handles a player's connection by injecting a custom handler. */
internal class PlayerConnectionHandler(
    private val packetApi: MinecraftPacketApi,
    private val player: Player,
) {
    private companion object {
        private const val NOXCREW_PACKET_HANDLER_KEY = "noxcrew_packet_handler"
        private const val MINECRAFT_PACKET_HANDLER_KEY = "packet_handler"

        private val logger = LoggerFactory.getLogger("PlayerConnectionHandler")
    }

    private var registered: Boolean = false

    /** Registers this handler, hooking into the pipeline. */
    fun register() {
        if (registered) return
        registered = true
        getPipeline().addBefore(
            MINECRAFT_PACKET_HANDLER_KEY,
            NOXCREW_PACKET_HANDLER_KEY,
            NoxcrewPacketHandler(packetApi, player),
        )
    }

    /** Unregisters this handler, removing the interceptor from the pipeline if not on disconnect. */
    fun unregister(disconnect: Boolean = false) {
        if (!registered) return
        registered = false

        if (!disconnect) {
            // Swallow the exception here as it just means the player wasn't properly set up yet
            try {
                getPipeline().remove(NOXCREW_PACKET_HANDLER_KEY)
            } catch (exception: Exception) {
                if (exception !is NoSuchElementException) {
                    logger.error("An unknown error occurred whilst removing a packet handler from a player", exception)
                }
            }
        }
    }

    /** Returns the channel pipeline for this player. */
    private fun getPipeline(): ChannelPipeline = (player as CraftPlayer)
        .handle.connection.connection.channel
        .pipeline()
}
