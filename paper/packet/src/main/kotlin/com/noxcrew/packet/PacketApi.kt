package com.noxcrew.packet

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent
import com.google.common.collect.Multimap
import io.papermc.paper.connection.DisconnectionReason
import io.papermc.paper.connection.PlayerCommonConnection
import io.papermc.paper.connection.ReadablePlayerCookieConnectionImpl
import io.papermc.paper.event.connection.configuration.PlayerConnectionInitialConfigureEvent
import net.minecraft.ChatFormatting
import net.minecraft.network.Connection
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.server.network.ServerCommonPacketListenerImpl
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl
import net.minecraft.server.network.ServerGamePacketListenerImpl
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import net.minecraft.world.entity.player.Player as NmsPlayer

/**
 * A function handling an incoming or outgoing packet.
 *
 * The first argument is the player it's being sent to, the second argument is the packet.
 *
 * The returned packet will be passed on in the chain of packet handlers, and the last handler's
 * result will be passed on to the receiver. If any handler in the chain returns null, handling is
 * aborted immediately and no packet is sent.
 */
public fun interface PacketHandlerFunction<R, T : Packet<*>> : (R, T) -> List<Packet<*>?>

/**
 * Holds a set of packet handlers for a given packet type.
 */
private data class PacketHandlers<P : Packet<*>>(private val packet: Class<P>) {
    private val lock: ReentrantReadWriteLock = ReentrantReadWriteLock()

    /**
     * This uses an arraylist for fast queries. We insert far less frequently than we access, so
     * access speed should definitely be prioritized.
     */
    private val handlers: MutableList<PacketHandlerWithPriority<*, P>> = ArrayList()

    operator fun plusAssign(handler: PacketHandlerWithPriority<*, P>) {
        lock.write {
            handlers += handler
            handlers.sort()
        }
    }

    operator fun minusAssign(handler: PacketHandlerWithPriority<*, P>) {
        lock.write {
            handlers -= handler
            handlers.sort()
        }
    }

    /** Reads the handlers, ensuring no additions/removals are done whilst [operation] executes. */
    @OptIn(ExperimentalContracts::class)
    inline fun read(operation: (List<PacketHandlerWithPriority<*, P>>) -> Unit) {
        contract {
            callsInPlace(operation, InvocationKind.EXACTLY_ONCE)
        }
        lock.read { operation(handlers) }
    }
}

/**
 * Assigns a priority to a [PacketHandlerFunction], so that packet handlers can be sorted in
 * collections based on their priority.
 *
 * This is not a data class, because we only want to compare identifier. `fun interface` has no real
 * identity if inlined.
 */
private class PacketHandlerWithPriority<R, T : Packet<*>>(
    val handler: PacketHandlerFunction<R, T>,
    val receiverType: Class<R>,
    val priority: Int,
    val identifier: UUID = UUID.randomUUID(),
) : Comparable<PacketHandlerWithPriority<*, *>> {
    /** Handles the given [packet] for [connection]. */
    @Suppress("UNCHECKED_CAST")
    fun handle(connection: Connection, packet: Packet<*>): List<Packet<*>?> {
        // Cast the packet and connection to the right types used by this method
        val castPacket = packet as? T ?: return listOf(packet)
        val receiver =
            when {
                Player::class.java.isAssignableFrom(receiverType) -> connection.player?.bukkitEntity
                NmsPlayer::class.java.isAssignableFrom(receiverType) -> connection.player
                PlayerCommonConnection::class.java.isAssignableFrom(receiverType) ->
                    (connection.packetListener as? ServerConfigurationPacketListenerImpl)?.paperConnection
                        ?: (connection.packetListener as? ServerGamePacketListenerImpl)?.paperConnection()

                else -> return listOf(packet)
            } as? R ?: return listOf(packet)
        return handler(receiver, castPacket)
    }

    override fun compareTo(other: PacketHandlerWithPriority<*, *>): Int = priority.compareTo(other.priority)

    override fun equals(other: Any?): Boolean = this === other || identifier == (other as? PacketHandlerWithPriority<*, *>)?.identifier

    override fun hashCode(): Int = identifier.hashCode()
}

/** A function that unregisters a packet handler. */
public typealias PacketHandlerUnregisterer = () -> Unit

/** Provides the basis for a packet listening, modification, and cancellation API. */
public class PacketApi(
    /** The unique key to use for the packet handler. */
    public val key: String,
    /** Whether to kick players when packet handlers throw errors. */
    public val kickForPacketErrors: Boolean = true,
) : Listener {
    public companion object {
        /**
         * The default priority of a packet handler.
         */
        public const val DEFAULT_HANDLER_PRIORITY: Int = 100
    }

    private val connectionField =
        ReadablePlayerCookieConnectionImpl::class.java.getDeclaredField("connection").also {
            it.isAccessible = true
        }
    private val logger = LoggerFactory.getLogger("PacketApi")
    private val playerConnectionHandlers = mutableMapOf<UUID, PlayerConnectionHandler>()
    private val packetHandlers: MutableMap<Class<*>, PacketHandlers<*>> = ConcurrentHashMap()
    private var plugin: Plugin? = null

    /** For each registered [PacketListener], unregisterers for all of its packet handlers. */
    private val listenerUnregisterers: Multimap<PacketListener, PacketHandlerUnregisterer> = ThreadsafeMultimap()

    /** Whether this api has been registered. */
    public var registered: Boolean = false
        private set

    /** Registers the packet API. */
    public fun register(plugin: Plugin) {
        if (registered) return
        registered = true

        this.plugin = plugin
        Bukkit.getPluginManager().registerEvents(this, plugin)
        Bukkit.getOnlinePlayers().forEach(::registerPlayer)
    }

    /** Unregisters the packet API. */
    public fun unregister() {
        if (!registered) return
        registered = false

        HandlerList.unregisterAll(this)
        for (player in playerConnectionHandlers.keys.toSet()) {
            unregisterPlayer(player, false)
        }
    }

    /**
     * Registers a packet [handler] for the specified packet [type]. The [priority] decides when
     * the handler is called relative to other packet handlers - handlers with a lower [priority]
     * are called before those with a higher priority.
     *
     * Returns a [PacketHandlerUnregisterer] that unregisters the listener when called.
     */
    @Suppress("UNCHECKED_CAST")
    public fun <R, T : Packet<*>> registerHandler(
        type: Class<T>,
        receiver: Class<R>,
        priority: Int = DEFAULT_HANDLER_PRIORITY,
        handler: PacketHandlerFunction<R, T>,
    ): PacketHandlerUnregisterer {
        val handlerWithPriority = PacketHandlerWithPriority(handler, receiver, priority)
        val handlers = packetHandlers.computeIfAbsent(type) { PacketHandlers(type) } as PacketHandlers<T>

        // Register the handler.
        handlers += handlerWithPriority

        return {
            // Un-register the handler.
            handlers -= handlerWithPriority
        }
    }

    /**
     * Registers an packet [handler] for packets of type [T]. The [priority] decides when the
     * handler is called relative to other packet handlers - handlers with a lower [priority] are
     * called before those with a higher priority. The [priority] should not be negative.
     *
     * Returns a [PacketHandlerUnregisterer] that unregisters the listener when called.
     */
    public inline fun <reified R, reified T : Packet<*>> registerHandler(
        priority: Int = DEFAULT_HANDLER_PRIORITY,
        handler: PacketHandlerFunction<R, T>,
    ): PacketHandlerUnregisterer = registerHandler(T::class.java, R::class.java, priority, handler)

    /** Returns whether handlers for packets of the given [clazz] are registered. */
    public fun hasHandlers(clazz: Class<out Packet<*>>): Boolean =
        packetHandlers.containsKey(clazz) || clazz == ClientboundBundlePacket::class.java

    /** Calls all handlers on the given [packet], returning the manipulated packet. */
    internal fun handlePacket(connection: Connection, packet: Packet<*>): List<Packet<*>> {
        // Early-exit if this type has no handlers!
        val baseType = packet.javaClass
        if (!hasHandlers(baseType)) return listOf(packet)

        val finalPackets = mutableListOf<Packet<*>>()
        var pendingPackets = HashMap<Class<in Packet<*>>, MutableList<Packet<*>>>()
        val checkedTypes = mutableSetOf<Class<*>>()

        // Queue up the initial packet
        pendingPackets[baseType] = mutableListOf(packet)

        // Iterate across all packets to be processed in a BFS.
        while (pendingPackets.isNotEmpty()) {
            val currentPackets = pendingPackets
            val typesCheckedThisLayer = mutableListOf<Class<*>>()
            pendingPackets = HashMap()

            while (currentPackets.isNotEmpty()) {
                val type = currentPackets.keys.firstOrNull() ?: break
                var activePackets = currentPackets.remove(type) ?: break

                // If this packet type has already been checked, it's finished!
                if (type in checkedTypes) {
                    finalPackets += activePackets

                    // If we got here there were two handlers creating each other's type, this would
                    // cause infinite packet copies to be made, this should not happen!
                    logger.warn("Skipping packet handling of packet type $type due to nested handlers")
                    continue
                }

                // If it's a bundle, unpack into the next group of packets!
                if (type == ClientboundBundlePacket::class.java) {
                    for (bundlePacket in activePackets) {
                        for (packet in (bundlePacket as? ClientboundBundlePacket ?: continue).subPackets()) {
                            if (packet != null) {
                                pendingPackets.getOrPut(packet.javaClass) { mutableListOf() } += packet
                            }
                        }
                    }
                    continue
                }

                // Go through all packet handlers of this type of packet
                packetHandlers[type]?.read { handlers ->
                    for (handlerWithPriority in handlers) {
                        val iteratorPackets = activePackets
                        activePackets = mutableListOf()

                        for (iteratorPacket in iteratorPackets) {
                            val output =
                                try {
                                    // If there is no active packet anymore, stop running these handlers!
                                    handlerWithPriority.handle(connection, iteratorPacket)
                                } catch (e: Exception) {
                                    logger.error(
                                        "Error handling packet ${packet.javaClass.simpleName}, kicking player!",
                                        e,
                                    )

                                    if (kickForPacketErrors) {
                                        // If an error occurs, kick the player on the main thread!
                                        plugin?.also {
                                            Bukkit.getScheduler().callSyncMethod(it) {
                                                (connection.packetListener as? ServerCommonPacketListenerImpl)?.disconnect(
                                                    Component
                                                        .literal("An error occurred while parsing packets")
                                                        .withStyle(ChatFormatting.RED),
                                                    DisconnectionReason.INVALID_PAYLOAD,
                                                )
                                            }
                                            return@read
                                        }
                                    }
                                    continue
                                }

                            // Look through the output packets and add them to the correct lists
                            for (packet in output) {
                                // Filter out null packets!
                                if (packet == null) continue

                                val newType = packet.javaClass
                                if (newType == type) {
                                    // The type is the same, continue iterating with it!
                                    activePackets += packet
                                } else {
                                    // If this new type does not have a packet handler, early exit!
                                    if (!hasHandlers(newType)) {
                                        finalPackets += packet
                                        continue
                                    }

                                    // If the type has changed, iterate over this on the next layer!
                                    pendingPackets.getOrPut(newType) { mutableListOf() } += packet
                                }
                            }
                        }
                    }
                }

                // We got through the handlers nicely, these packets are finished!
                finalPackets += activePackets
            }

            // Only allow each packet type to be checked once per layer,
            // this means if we have a bundle of the same packet we run the
            // handler on each of those packets separately, but if that causes
            // another nested bundle packet of the same type we no longer call
            // listeners. This avoids infinite growth of the space to check.
            checkedTypes += typesCheckedThisLayer
        }
        return finalPackets
    }

    /**
     * Registers all methods on the given [listener] that have the [PacketHandler] annotation as
     * packet handlers. Private methods as well as methods on superclasses are respected.
     *
     * @see registerHandler
     * @see unregisterListener
     */
    public fun registerListener(listener: PacketListener) {
        var clazz: Class<*> = listener.javaClass

        while (PacketListener::class.java.isAssignableFrom(clazz)) {
            // find methods with the PacketHandler annotation
            for (method in clazz.declaredMethods) {
                method.isAccessible = true
                val annotation = method.getAnnotation(PacketHandler::class.java) ?: continue

                // method must satisfy the [PacketHandlerFunction] interface
                require(
                    method.parameters.size == 2 &&
                        (
                            Player::class.java.isAssignableFrom(method.parameterTypes[0]) ||
                                NmsPlayer::class.java.isAssignableFrom(method.parameterTypes[0]) ||
                                PlayerCommonConnection::class.java.isAssignableFrom(method.parameterTypes[0])
                        ) &&
                        Packet::class.java.isAssignableFrom(method.parameterTypes[1]) &&
                        // Allow you to return one or multiple packets
                        (
                            Packet::class.java.isAssignableFrom(method.returnType) ||
                                List::class.java.isAssignableFrom(
                                    method.returnType,
                                )
                        ),
                ) {
                    "PacketHandler $method on $clazz doesn't match the PacketHandlerFunction interface (2 parameters, player and packet, returns packet or list of packets)"
                }
                val multiple = List::class.java.isAssignableFrom(method.returnType)

                @Suppress("UNCHECKED_CAST")
                val unregisterer =
                    registerHandler(
                        method.parameterTypes[1] as Class<Packet<*>>,
                        method.parameterTypes[0],
                        annotation.priority,
                    ) { player, packet ->
                        if (multiple) {
                            method.invoke(listener, player, packet) as List<Packet<*>>
                        } else {
                            val result = method.invoke(listener, player, packet)
                            when (result) {
                                null -> {
                                    // Ignore result if returned value is null
                                    emptyList()
                                }

                                is ClientboundBundlePacket -> {
                                    // Turn bundles into a list of packets so they can be properly handled!
                                    result.subPackets().toList()
                                }

                                else -> {
                                    // Wrap singular packets into a list so we can support both nicely
                                    listOf(result as Packet<*>)
                                }
                            }
                        }
                    }

                // store the unregisterer
                listenerUnregisterers.put(listener, unregisterer)
            }

            // traverse the class hierarchy upwards
            // until the PacketListener interface is reached
            clazz = clazz.superclass
        }
    }

    /**
     * Unregisters all the given [listener]'s packet handlers that were previously registered
     * using [registerListener].
     *
     * @see registerListener
     */
    public fun unregisterListener(listener: PacketListener) {
        listenerUnregisterers.removeAll(listener).forEach(PacketHandlerUnregisterer::invoke)
    }

    /** Registers a connection handler for a new [player]. */
    private fun registerPlayer(player: Player) {
        val connection = (player as CraftPlayer).handle.connection.connection
        registerConnection(player.uniqueId, connection)
    }

    /** Registers a connection handler for a new [connection] for [playerUUID]. */
    private fun registerConnection(playerUUID: UUID, connection: Connection) {
        val handler = PlayerConnectionHandler(key, this, connection)
        playerConnectionHandlers.put(playerUUID, handler)?.unregister()
        handler.register()
    }

    /** Unregisters the connection handler for [playerUUID]. */
    private fun unregisterPlayer(playerUUID: UUID, disconnect: Boolean = false) {
        playerConnectionHandlers.remove(playerUUID)?.unregister(disconnect)
    }

    @EventHandler
    public fun onPlayerConfigure(event: PlayerConnectionInitialConfigureEvent) {
        try {
            val connection = connectionField.get(event.connection) as Connection
            registerConnection(event.connection.profile.uniqueId ?: return, connection)
        } catch (exception: Exception) {
            logger.warn("Failed to set up interceptor for player ${event.connection.profile.name}", exception)
        }
    }

    @EventHandler
    public fun onPlayerQuit(event: PlayerQuitEvent) {
        try {
            unregisterPlayer(event.player.uniqueId, disconnect = true)
        } catch (exception: Exception) {
            logger.warn("Failed to remove interceptor for player ${event.player.name}", exception)
        }
    }

    @EventHandler
    public fun onPlayerDisconnected(event: PlayerConnectionCloseEvent) {
        try {
            unregisterPlayer(event.playerUniqueId, disconnect = true)
        } catch (exception: Exception) {
            logger.warn("Failed to remove interceptor for player ${event.playerName}", exception)
        }
    }
}

/** Sends the given [packets] to [this] player. */
public fun Player.sendPacket(vararg packets: Packet<*>?) {
    val connection = (this as? CraftPlayer)?.handle?.connection ?: return
    for (packet in packets) {
        connection.send(packet ?: continue)
    }
}

/** Sends the given [packets] to [this] collection of players. */
public fun Collection<Player>.sendPacket(vararg packets: Packet<*>?): Unit = forEach {
    it.sendPacket(packets.toList())
}

/** Sends the given [packets] to [this] player. */
public fun Player.sendPacket(packets: Iterable<Packet<*>?>) {
    val connection = (this as? CraftPlayer)?.handle?.connection ?: return
    for (packet in packets) {
        connection.send(packet ?: continue)
    }
}

/** Sends the given [packets] to [this] collection of players. */
public fun Iterable<Player>.sendPacket(packets: Iterable<Packet<*>>): Unit = forEach {
    it.sendPacket(packets)
}
