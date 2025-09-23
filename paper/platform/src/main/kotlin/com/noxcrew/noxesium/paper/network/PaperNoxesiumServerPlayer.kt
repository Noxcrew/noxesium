package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.network.ConnectionProtocolType
import com.noxcrew.noxesium.api.network.handshake.HandshakePackets
import com.noxcrew.noxesium.api.network.handshake.HandshakeState
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import com.noxcrew.noxesium.api.player.SerializedNoxesiumServerPlayer
import io.papermc.paper.connection.PaperCommonConnection
import io.papermc.paper.connection.PaperPlayerConfigurationConnection
import io.papermc.paper.connection.PlayerCommonConnection
import io.papermc.paper.connection.PlayerConfigurationConnection
import io.papermc.paper.connection.PlayerConnection
import io.papermc.paper.connection.PlayerGameConnection
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.common.custom.DiscardedPayload
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl
import net.minecraft.server.network.ServerGamePacketListenerImpl
import org.bukkit.craftbukkit.entity.CraftPlayer
import java.io.ByteArrayOutputStream
import java.util.UUID

/** A variant of a Noxesium player that holds a reference to a server player. */
public class PaperNoxesiumServerPlayer(
    private val parent: PaperNoxesiumServerHandshaker,
    uniqueId: UUID,
    username: String,
    initialConnection: PlayerConnection,
    serializedPlayer: SerializedNoxesiumServerPlayer? = null,
) : NoxesiumServerPlayer(uniqueId, username, Component.text(username), serializedPlayer) {
    private val _registeredPluginChannels = mutableSetOf<String>()
    private val declaredField =
        PaperCommonConnection::class.java.getDeclaredField("handle").also {
            it.isAccessible = true
        }

    // Start out by awaiting to send the handshake packets to the client!
    private var pendingPluginChannels = HandshakePackets.INSTANCE.pluginChannelIdentifiers.toMutableSet()
    private var firstHandshake = true

    /** The current connection type of this player. */
    public var connectionType: ConnectionProtocolType = ConnectionProtocolType.NONE

    /** The current connection object for this player. */
    public var connection: PlayerConnection? = initialConnection
        set(value) {
            if (field == value) return
            field = value

            println("connection is now $connection")

            // Update the type of connection this player currently has
            connectionType =
                when (value) {
                    is PlayerCommonConnection -> ConnectionProtocolType.CONFIGURATION
                    is PlayerGameConnection -> ConnectionProtocolType.PLAY
                    else -> ConnectionProtocolType.NONE
                }

            // Start transferring if this is the first time we're able to
            if (handshakeState == HandshakeState.NONE && connectionType != ConnectionProtocolType.NONE) {
                // Try to perform a transfer at most once, as afterward the client won't be assumed
                // to be done with the handshake!
                val transfer = firstHandshake && isTransferred
                firstHandshake = false
                if (transfer) {
                    parent.handleTransfer(this)
                }
            }

            // Send any queued packets on the new connection
            sendQueuedPackets()
        }

    /** Returns whether this player is still connected. */
    public val isConnected: Boolean
        get() =
            connection != null &&
                when (connection) {
                    // Check if the underlying player is still connected!
                    is PlayerGameConnection -> (connection as PlayerGameConnection).player.isConnected
                    else -> true
                }

    /** All plugin channels registered with this player. */
    public val registeredPluginChannels: Collection<String>
        get() = _registeredPluginChannels

    override fun getDisplayName(): Component = when (connection) {
        // Read the display name off the current connection if possible!
        is PlayerGameConnection -> (connection as PlayerGameConnection).player.displayName()
        else -> super.displayName
    }

    /** Registers a new plugin channel. */
    public fun registerPluginChannels(vararg channels: String): Unit = registerPluginChannels(channels.toSet())

    /** Registers a new plugin channel. */
    public fun registerPluginChannels(channels: Collection<String>) {
        pendingPluginChannels += channels
    }

    /**
     * Sends the given [packet] to this player.
     */
    public fun sendPayload(packet: ClientboundCustomPayloadPacket) {
        val handle = if (connection is PlayerConfigurationConnection) {
            val conn = connection as PaperPlayerConfigurationConnection
            (declaredField.get(conn) as ServerConfigurationPacketListenerImpl)
        } else if (connection is PlayerGameConnection) {
            ((connection as PlayerGameConnection).player as CraftPlayer).handle.connection
        } else {
            throw IllegalStateException("Invalid connection type for sending payloads ${connection?.javaClass}")
        }

        // Validate that the connection hasn't been destroyed yet!
        if (handle.connection.channel.)

        println("queued up $packet")
        handle.send(packet)
    }

    /** Sends this player a request to register the given [channels]. */
    private fun sendPluginChannels(channels: Collection<String>) {
        if (channels.isEmpty()) return

        val stream = ByteArrayOutputStream()
        for (channel in channels) {
            stream.write(channel.toByteArray(Charsets.UTF_8))
            stream.write(0)
        }
        sendPayload(
            ClientboundCustomPayloadPacket(
                DiscardedPayload(
                    ServerGamePacketListenerImpl.CUSTOM_REGISTER,
                    stream.toByteArray(),
                ),
            ),
        )
    }

    /** Sends any queued packets. */
    private fun sendQueuedPackets() {
        // Do not send anything until we have a connection!
        if (connectionType == ConnectionProtocolType.NONE) return

        // Send the client new plugin channels that it does not yet know about
        if (pendingPluginChannels.isNotEmpty()) {
            val newChannels = pendingPluginChannels
            pendingPluginChannels = mutableSetOf()
            sendPluginChannels(newChannels)
            _registeredPluginChannels += newChannels
        }
    }
}
