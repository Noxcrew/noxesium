package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.network.handshake.HandshakePackets
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import com.noxcrew.noxesium.api.player.SerializedNoxesiumServerPlayer
import io.papermc.paper.connection.PaperCommonConnection
import io.papermc.paper.connection.PaperPlayerConfigurationConnection
import io.papermc.paper.connection.PlayerCommonConnection
import io.papermc.paper.connection.PlayerConfigurationConnection
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
    uniqueId: UUID,
    username: String,
    /** The connection instance. */
    public var connection: PlayerCommonConnection?,
    serializedPlayer: SerializedNoxesiumServerPlayer? = null,
) : NoxesiumServerPlayer(uniqueId, username, Component.text(username), serializedPlayer) {
    // All players know about the handshake packets by default!
    private val _registeredPluginChannels = HandshakePackets.INSTANCE.pluginChannelIdentifiers.toMutableSet()
    private val declaredField =
        PaperCommonConnection::class.java.getDeclaredField("handle").also {
            it.isAccessible = true
        }

    private var pendingPluginChannels = mutableSetOf<String>()

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
        if (connection is PlayerConfigurationConnection) {
            val conn = connection as PaperPlayerConfigurationConnection
            val handle = declaredField.get(conn) as ServerConfigurationPacketListenerImpl
            handle.send(packet)
        } else if (connection is PlayerGameConnection) {
            ((connection as PlayerGameConnection).player as CraftPlayer).handle.connection.send(packet)
        } else {
            throw IllegalStateException("Invalid connection type ${connection?.javaClass}")
        }
    }

    /** Sends this player a request to register the given [channels]. */
    public fun sendPluginChannels(channels: Collection<String>) {
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

    override fun tick() {
        super.tick()

        // Send the client new plugin channels that it does not yet know about
        if (pendingPluginChannels.isEmpty()) return
        val newChannels = pendingPluginChannels
        pendingPluginChannels = mutableSetOf()
        sendPluginChannels(newChannels)
        _registeredPluginChannels += newChannels
    }
}
