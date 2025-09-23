package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.network.ConnectionProtocolType
import com.noxcrew.noxesium.api.network.handshake.HandshakePackets
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import com.noxcrew.noxesium.api.player.SerializedNoxesiumServerPlayer
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.common.custom.DiscardedPayload
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import java.io.ByteArrayOutputStream

/** A variant of a Noxesium player that holds a reference to a server player. */
public class PaperNoxesiumServerPlayer(
    /** The wrapped player instance. */
    public val player: ServerPlayer,
    serializedPlayer: SerializedNoxesiumServerPlayer? = null,
) : NoxesiumServerPlayer(player.uuid, player.gameProfile.name, player.`adventure$displayName`, serializedPlayer) {
    // Start out by having all handshake channels as known by the client
    private val _registeredPluginChannels = HandshakePackets.INSTANCE.pluginChannelIdentifiers.toMutableSet()
    private var pendingPluginChannels = mutableSetOf<String>()

    /** The current connection type of this player. */
    public var connectionType: ConnectionProtocolType = ConnectionProtocolType.NONE

    /** Returns whether this player is still connected. */
    public val isConnected: Boolean
        get() = player.bukkitEntity.isConnected

    /** All plugin channels registered with this player. */
    public val registeredPluginChannels: Collection<String>
        get() = _registeredPluginChannels

    /** Registers a new plugin channel. */
    public fun registerPluginChannels(vararg channels: String): Unit = registerPluginChannels(channels.toSet())

    /** Registers a new plugin channel. */
    public fun registerPluginChannels(channels: Collection<String>) {
        pendingPluginChannels += channels
    }

    override fun tick() {
        super.tick()

        // Do not send anything until we have a connection!
        if (!isConnected) return

        // Send the client new plugin channels that it does not yet know about
        if (pendingPluginChannels.isNotEmpty()) {
            val newChannels = pendingPluginChannels
            pendingPluginChannels = mutableSetOf()
            player.sendPluginChannels(newChannels)
            _registeredPluginChannels += newChannels
        }
    }
}

/** Sends the given [packet] to this player. */
public fun ServerPlayer.sendPayload(packet: ClientboundCustomPayloadPacket) {
    if (!bukkitEntity.isConnected) throw IllegalStateException("Cannot send packet when not connected")
    connection.send(packet)
}

/** Sends this player a request to register the given [channels]. */
public fun ServerPlayer.sendPluginChannels(channels: Collection<String>) {
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
