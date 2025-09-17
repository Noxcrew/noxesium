package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.NoxesiumApi
import com.noxcrew.noxesium.api.NoxesiumReferences
import com.noxcrew.noxesium.api.network.EntrypointProtocol
import com.noxcrew.noxesium.api.network.NoxesiumClientboundNetworking
import com.noxcrew.noxesium.api.network.handshake.HandshakePackets
import com.noxcrew.noxesium.api.network.handshake.HandshakeState
import com.noxcrew.noxesium.api.network.handshake.NoxesiumServerHandshaker
import com.noxcrew.noxesium.api.network.json.JsonSerializedPacket
import com.noxcrew.noxesium.api.network.json.JsonSerializerRegistry
import com.noxcrew.noxesium.api.nms.serialization.PacketSerializerRegistry
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import com.noxcrew.noxesium.api.player.SerializedNoxesiumServerPlayer
import com.noxcrew.noxesium.paper.NoxesiumPaper
import com.noxcrew.noxesium.paper.api.event.NoxesiumPlayerRegisteredEvent
import com.noxcrew.noxesium.paper.api.event.NoxesiumPlayerUnregisteredEvent
import com.noxcrew.noxesium.paper.feature.noxesiumPlayer
import com.noxcrew.packet.PacketHandler
import com.noxcrew.packet.PacketListener
import io.netty.buffer.Unpooled
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
import net.minecraft.network.protocol.common.custom.DiscardedPayload
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRegisterChannelEvent
import org.bukkit.event.player.PlayerUnregisterChannelEvent
import java.util.UUID
import java.util.logging.Level

/**
 * Performs handshaking with the server-side to be run from the client-side.
 */
public open class PaperNoxesiumServerHandshaker : NoxesiumServerHandshaker(), Listener, PacketListener {
    override fun register() {
        super.register()
        Bukkit.getPluginManager().registerEvents(this, NoxesiumPaper.plugin)
        NoxesiumPaper.packetApi.registerListener(this)

        // Tick all players and detect handshake completions after channels are created
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
            NoxesiumPaper.plugin,
            {
                for (player in NoxesiumPlayerManager.getInstance().allPlayers) {
                    player.tick()

                    // Test if all handshake tasks are already complete
                    if (player.isHandshakeCompleted()) {
                        completeHandshake(player)
                    }
                }
            },
            1, 1,
        )

        // Respond to initial handshake packet and create the server player instance
        HandshakePackets.SERVERBOUND_HANDSHAKE.addListener(
            this,
        ) { reference, packet, playerId ->
            if (NoxesiumPlayerManager.getInstance().getPlayer(playerId) != null) {
                NoxesiumApi.getLogger().error("Received registry contents while player was known, destroying connection!")
                destroy(playerId)
                return@addListener
            }

            val bukkitPlayer = Bukkit.getPlayer(playerId) as CraftPlayer
            val serverPlayer = bukkitPlayer.handle
            reference.handleHandshake(PaperNoxesiumServerPlayer(serverPlayer), packet!!)
        }
    }

    override fun tick() {
        super.tick()

        // Store any players whose serialized data has changed to an external database
        NoxesiumPlayerManager
            .getInstance()
            .allPlayers
            .filter { it.isDirty }
            .forEach {
                // Only store players who have completed the handshake!
                if (it.handshakeState == HandshakeState.COMPLETE) {
                    storeData(it)
                }
                it.unmarkDirty()
            }
    }

    /**
     * When a player finishes joining, attempt to load any previous stored data from an
     * external database. If this data is present, perform a partial handshake from the
     * server-side.
     */
    @EventHandler
    public fun onPlayerJoin(event: PlayerJoinEvent) {
        // Ignore if already handshaken!
        val playerId = event.player.uniqueId
        if (NoxesiumPlayerManager.getInstance().getPlayer(playerId) != null) return

        // Try to handle a transfer from another server
        val bukkitPlayer = event.player as CraftPlayer
        val serverPlayer = bukkitPlayer.handle
        getStoredData(event.player.uniqueId)?.also { storedData ->
            handleTransfer(PaperNoxesiumServerPlayer(serverPlayer, storedData))
            return@also
        }

        // If we're not transferring, send this player the initial signal by way of informing
        // them about the handshake plugin channels.
        serverPlayer.sendPluginChannels(HandshakePackets.INSTANCE.pluginChannelIdentifiers)
    }

    @EventHandler
    public fun onPlayerQuit(event: PlayerQuitEvent) {
        onPlayerDisconnect(event.player.uniqueId)
    }

    @EventHandler
    public fun onChannelRegistered(event: PlayerRegisterChannelEvent) {
        onChannelRegistered(event.player.noxesiumPlayer, event.channel)
    }

    @PacketHandler
    public fun onCustomPayload(player: Player, packet: ServerboundCustomPayloadPacket): ServerboundCustomPayloadPacket? {
        (packet.payload as? DiscardedPayload)?.also { payload ->
            // Ignore packets not for Noxesium!
            if (payload.id.namespace != NoxesiumReferences.PACKET_NAMESPACE) return@also

            // Determine the current Noxesium player and if they can send this type of packet
            val noxesiumPlayer = player.noxesiumPlayer as? PaperNoxesiumServerPlayer
            val knownChannels = noxesiumPlayer?.registeredPluginChannels ?: HandshakePackets.INSTANCE.pluginChannelIdentifiers
            val channel = payload.id.toString()
            if (channel !in knownChannels) {
                NoxesiumPaper.plugin.logger.log(
                    Level.WARNING,
                    "Received unauthorized plugin message on channel '$channel' for ${player.name}",
                )
                return null
            }

            // Determine the payload type for this packet if the client knows of it
            val networking = NoxesiumClientboundNetworking.getInstance() as? PaperNoxesiumClientboundNetworking
            val payloadType = networking?.getPayloadType(channel) ?: return null

            try {
                // Decode the message and let handlers handle it
                val craftPlayer = player as CraftPlayer
                val buffer = RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(payload.data), craftPlayer.handle.registryAccess())

                val payload = if (payloadType.jsonSerialized) {
                    val serializer = JsonSerializerRegistry.getInstance().getSerializer(payloadType.clazz.getAnnotation(JsonSerializedPacket::class.java).value)
                    serializer.decode(buffer.readUtf(), payloadType.clazz)
                } else {
                    val codec = PacketSerializerRegistry.getSerializers(payloadType)
                    codec.decode(buffer)
                }

                // Perform packet handling on the main thread
                Bukkit.getScheduler().callSyncMethod(NoxesiumPaper.plugin) {
                    payloadType.handle(player.uniqueId, payload)
                }
            } catch (x: Exception) {
                NoxesiumPaper.plugin.logger.log(
                    Level.WARNING,
                    "Failed to decode plugin message on channel '$channel' for ${player.name}",
                    x,
                )
            }

            // Always hide Noxesium packets from Bukkit!
            return null
        }
        return packet
    }

    override fun activateProtocol(player: NoxesiumServerPlayer, protocol: EntrypointProtocol) {
        super.activateProtocol(player, protocol)

        // Register all plugin channels with this user for the newly authenticated protocols
        val entrypoint = NoxesiumApi.getInstance().getEntrypoint(protocol.id) ?: return
        (player as? PaperNoxesiumServerPlayer)?.registerPluginChannels(
            entrypoint
                .packetCollections
                .flatMap { it.pluginChannelIdentifiers },
        )
    }

    override fun isConnected(player: NoxesiumServerPlayer): Boolean =
        super.isConnected(player) && !(player as PaperNoxesiumServerPlayer).player.hasDisconnected()

    override fun runDelayed(runnable: Runnable) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(NoxesiumPaper.plugin, runnable)
    }

    override fun completeHandshake(player: NoxesiumServerPlayer): Boolean = if (super.completeHandshake(player)) {
        // Emit an event for other systems to hook into
        Bukkit
            .getPluginManager()
            .callEvent(NoxesiumPlayerRegisteredEvent((player as PaperNoxesiumServerPlayer).player.bukkitEntity, player))

        // Store the player's data externally after handshake completion
        storeData(player)
        true
    } else {
        false
    }

    override fun onPlayerDisconnect(uuid: UUID) {
        val player = NoxesiumPlayerManager.getInstance().getPlayer(uuid)
        super.onPlayerDisconnect(uuid)
        if (player != null) {
            // Delete any stored data for this player
            removeStoredData(player)

            // Emit an event for other systems to hook into on unregistration
            Bukkit
                .getPluginManager()
                .callEvent(NoxesiumPlayerUnregisteredEvent((player as PaperNoxesiumServerPlayer).player.bukkitEntity, player))
        }
    }

    /**
     * Reads the stored data for the given [playerId] from some custom implemented database
     * like a Redis to store player data between different servers on a network.
     */
    protected open fun getStoredData(playerId: UUID): SerializedNoxesiumServerPlayer? = null

    /**
     * Stores the data for [player] in some external database so it is present when the player
     * connects to a different server within a network.
     */
    protected open fun storeData(player: NoxesiumServerPlayer) {
    }

    /**
     * Indicates that stored data for [player] should be deleted.
     */
    protected open fun removeStoredData(player: NoxesiumServerPlayer) {
    }
}
