package com.noxcrew.packet

import net.minecraft.network.Connection
import org.slf4j.LoggerFactory

/** Handles a player's connection by injecting a custom handler. */
internal class PlayerConnectionHandler(
    private val key: String,
    private val packetApi: PacketApi,
    private val connection: Connection,
) {
    private companion object {
        private const val MINECRAFT_PACKET_HANDLER_KEY = "packet_handler"

        private val logger = LoggerFactory.getLogger("PlayerConnectionHandler")
    }

    private var registered: Boolean = false

    /** Registers this handler, hooking into the pipeline. */
    fun register() {
        if (registered) return
        registered = true
        connection.channel.pipeline().addBefore(
            MINECRAFT_PACKET_HANDLER_KEY,
            key,
            ChannelPacketHandler(packetApi, connection),
        )
    }

    /** Unregisters this handler, removing the interceptor from the pipeline if not on disconnect. */
    fun unregister(disconnect: Boolean = false) {
        if (!registered) return
        registered = false

        if (!disconnect) {
            // Swallow the exception here as it just means the player wasn't properly set up yet
            try {
                connection.channel.pipeline().remove(key)
            } catch (exception: Exception) {
                if (exception !is NoSuchElementException) {
                    logger.error("An unknown error occurred whilst removing a packet handler from a player", exception)
                }
            }
        }
    }
}
