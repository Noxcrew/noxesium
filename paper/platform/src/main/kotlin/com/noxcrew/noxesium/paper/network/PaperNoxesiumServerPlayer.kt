package com.noxcrew.noxesium.paper.network

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
    /** The nms player instance. */
    public val player: ServerPlayer,
    serializedPlayer: SerializedNoxesiumServerPlayer? = null,
) : NoxesiumServerPlayer(player.uuid, player.gameProfile.name, player.`adventure$displayName`, serializedPlayer) {
    // All players know about the handshake packets by default!
    private val _registeredPluginChannels = HandshakePackets.INSTANCE.pluginChannelIdentifiers.toMutableSet()

    private var pendingPluginChannels = mutableSetOf<String>()

    /** All plugin channels registered with this player. */
    public val registeredPluginChannels: Collection<String>
        get() = _registeredPluginChannels

    /** Registers a new plugin channel. */
    public fun registerPluginChannel(channel: String) {
        if (channel in _registeredPluginChannels) return
        pendingPluginChannels += channel
    }

    override fun tick() {
        super.tick()

        // Send the client new plugin channels that it does not yet know about
        if (pendingPluginChannels.isEmpty()) return
        val newChannels = pendingPluginChannels
        pendingPluginChannels = mutableSetOf()
        player.sendPluginChannels(newChannels)
        _registeredPluginChannels += newChannels
    }
}

/** Sends this client a request to register the given [channels]. */
public fun ServerPlayer.sendPluginChannels(channels: Collection<String>) {
    val stream = ByteArrayOutputStream()
    for (channel in channels) {
        stream.write(channel.toByteArray(Charsets.UTF_8))
        stream.write(0)
    }
    connection.send(
        ClientboundCustomPayloadPacket(
            DiscardedPayload(
                ServerGamePacketListenerImpl.CUSTOM_REGISTER,
                stream.toByteArray(),
            ),
        ),
    )
}
