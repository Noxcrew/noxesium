package com.noxcrew.noxesium.paper.api

import com.noxcrew.noxesium.paper.api.network.NoxesiumPacket
import io.netty.buffer.Unpooled
import net.kyori.adventure.key.Key
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.common.custom.DiscardedPayload
import net.minecraft.resources.ResourceLocation
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.messaging.PluginMessageListener
import org.bukkit.plugin.messaging.StandardMessenger
import org.slf4j.Logger
import java.util.concurrent.atomic.AtomicBoolean

/** Forms the basis for a Noxesium listener. */
public abstract class BaseNoxesiumListener(
    public val plugin: Plugin,
    public val logger: Logger,
    public val manager: NoxesiumManager,
): Listener {

    public companion object {
        /** Base namespace of the plugin channel for Noxesium messages. */
        public const val NOXESIUM_NAMESPACE: String = "noxesium"
    }

    private val incomingPluginChannels = mutableMapOf<Key, PluginMessageListener>()
    private val outgoingPluginChannels = mutableSetOf<Key>()
    private val registered = AtomicBoolean(false)

    /**
     * Defines a plugin message [listener] that will be registered and unregistered with
     * this listener.
     */
    protected fun registerIncomingPluginChannel(channel: Key, listener: PluginMessageListener) {
        incomingPluginChannels[channel] = listener
        if (registered.get()) {
            plugin.server.messenger.registerIncomingPluginChannel(plugin, channel.asString(), listener)
        }
    }

    /**
     * Defines an outgoing plugin channel that will be registered and unregistered with
     * this listener.
     */
    protected fun registerOutgoingPluginChannel(channel: Key) {
        outgoingPluginChannels += channel
        if (registered.get()) {
            plugin.server.messenger.registerOutgoingPluginChannel(plugin, channel.asString())
        }
    }

    /** Registers this listener. */
    public fun register(): BaseNoxesiumListener {
        require(registered.compareAndSet(false, true)) { "Cannot register when registered" }
        Bukkit.getPluginManager().registerEvents(this, plugin)
        for ((channel, listener) in incomingPluginChannels) {
            plugin.server.messenger.registerIncomingPluginChannel(plugin, channel.asString(), listener)
        }
        for (channel in outgoingPluginChannels) {
            plugin.server.messenger.registerOutgoingPluginChannel(plugin, channel.asString())
        }
        return this
    }

    /** Un-registers this listener. */
    public fun unregister() {
        require(registered.compareAndSet(true, false)) { "Cannot un-register when not registered" }
        HandlerList.unregisterAll(this)
        for ((channel, listener) in incomingPluginChannels) {
            plugin.server.messenger.unregisterIncomingPluginChannel(plugin, channel.asString(), listener)
        }
        for (channel in outgoingPluginChannels) {
            plugin.server.messenger.unregisterOutgoingPluginChannel(plugin, channel.asString())
        }
    }

    /** Creates this packet for the given [player]. */
    public abstract fun createPacket(player: Player, packet: NoxesiumPacket): ClientboundCustomPayloadPacket?
}

/** Sends a plugin message to a player. */
public fun Player.createPayloadPacket(channel: Key, initialCapacity: Int? = null, writer: (buffer: RegistryFriendlyByteBuf) -> Unit): ClientboundCustomPayloadPacket? {
    val craftPlayer = this as CraftPlayer
    if (craftPlayer.handle.connection == null) return null
    if (channel.asString() in craftPlayer.listeningPluginChannels) {
        return ClientboundCustomPayloadPacket(
            DiscardedPayload(
                ResourceLocation.parse(StandardMessenger.validateAndCorrectChannel(channel.asString())),
                // We have to do this custom so we can re-use the byte buf otherwise it gets padded with 0's!
                RegistryFriendlyByteBuf(initialCapacity?.let(Unpooled::buffer) ?: Unpooled.buffer(), (Bukkit.getServer() as CraftServer).handle.server.registryAccess()).apply(writer)
            )
        )
    }
    Bukkit.getLogger().warning("Couldn't create packet of type $channel for player $name as they have not yet registered the required plugin channel")
    return null
}

/** Sends a plugin message to a player. */
public fun Player.sendPluginMessage(channel: Key, initialCapacity: Int? = null, writer: (buffer: RegistryFriendlyByteBuf) -> Unit) {
    val packet = createPayloadPacket(channel, initialCapacity, writer)
    if (packet != null) {
        val craftPlayer = this as CraftPlayer
        craftPlayer.handle.connection.send(packet)
    }
}

/** Reads a byte array using [reader]. */
public fun <T> ByteArray.readPluginMessage(reader: (buffer: RegistryFriendlyByteBuf) -> T): T =
    RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(this), (Bukkit.getServer() as CraftServer).handle.server.registryAccess()).let(reader)