package com.noxcrew.noxesium.paper.network

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent
import com.noxcrew.noxesium.api.NoxesiumApi
import com.noxcrew.noxesium.api.network.EntrypointProtocol
import com.noxcrew.noxesium.api.network.NoxesiumErrorReason
import com.noxcrew.noxesium.api.network.handshake.HandshakePackets
import com.noxcrew.noxesium.api.network.handshake.HandshakeState
import com.noxcrew.noxesium.api.network.handshake.NoxesiumServerHandshaker
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakePacket
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import com.noxcrew.noxesium.api.player.SerializedNoxesiumServerPlayer
import com.noxcrew.noxesium.paper.NoxesiumPaper
import com.noxcrew.noxesium.paper.api.event.NoxesiumPlayerAddedToWorldEvent
import com.noxcrew.noxesium.paper.api.event.NoxesiumPlayerRegisteredEvent
import com.noxcrew.noxesium.paper.api.event.NoxesiumPlayerUnregisteredEvent
import com.noxcrew.noxesium.paper.feature.noxesiumPlayer
import com.noxcrew.packet.PacketListener
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRegisterChannelEvent
import java.util.UUID

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
            ServerboundHandshakePacket::class.java,
        ) { reference, packet, playerId ->
            if (NoxesiumPlayerManager.getInstance().getPlayer(playerId) != null) {
                NoxesiumApi.getLogger().error("Received handshake attempt while player was known, destroying connection!")
                destroy(playerId, NoxesiumErrorReason.INVALID_STATE)
                return@addListener
            }

            val player = (Bukkit.getPlayer(playerId) as? CraftPlayer)?.handle
            if (player == null) {
                NoxesiumApi.getLogger().error("Received handshake for unknown player $playerId, destroying connection!")
                destroy(playerId, NoxesiumErrorReason.CLIENT_UNKNOWN)
                return@addListener
            }

            val noxesiumPlayer = PaperNoxesiumServerPlayer(player, serializedPlayer = getStoredData(playerId))
            reference.handleHandshake(noxesiumPlayer, packet!!)
        }
    }

    override fun tick() {
        super.tick()

        NoxesiumPlayerManager
            .getInstance()
            .allPlayers
            .forEach {
                // Store any players whose serialized data has changed to an external database
                if (it.isDirty) {
                    // Only store players who have completed the handshake!
                    if (it.handshakeState == HandshakeState.COMPLETE) {
                        storeData(it)
                    }
                    it.unmarkDirty()
                }

                // End handshaking if the last packet received was 5 seconds ago
                if (it.handshakeState != HandshakeState.COMPLETE && (System.currentTimeMillis() - it.lastPacketReceiveTime >= 5000)) {
                    destroy(it.uniqueId, NoxesiumErrorReason.TIMEOUT)
                }
            }
    }

    /** Perform a transfer if a player joins with existing data present. */
    @EventHandler
    public fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = (event.player as? CraftPlayer)?.handle ?: return

        // If there's already a player registered, set the player instance again! This is probably because
        // they were made to re-enter the config phase.
        (NoxesiumPlayerManager.getInstance().getPlayer(event.player.uniqueId) as? PaperNoxesiumServerPlayer)?.also { noxesiumPlayer ->
            noxesiumPlayer.player = player

            // Emit a new added to world event
            Bukkit
                .getPluginManager()
                .callEvent(NoxesiumPlayerAddedToWorldEvent(player.bukkitEntity, noxesiumPlayer))
            return
        }

        // If the player has stored data perform a transfer
        getStoredData(event.player.uniqueId)?.also { storedData ->
            handleTransfer(PaperNoxesiumServerPlayer(player, serializedPlayer = storedData))
            return
        }

        // If the player is new, inform them they can authenticate!
        player.sendPluginChannels(HandshakePackets.INSTANCE.pluginChannelIdentifiers)
    }

    /** When the player quits, unassign their player data as they may be moving into the configuration phase. */
    @EventHandler
    public fun onPlayerQuit(event: PlayerQuitEvent) {
        (NoxesiumPlayerManager.getInstance().getPlayer(event.player.uniqueId) as? PaperNoxesiumServerPlayer)?.player = null
    }

    /** Destroy the data if the connection is closed. */
    @EventHandler
    public fun onPlayerConnectionClose(event: PlayerConnectionCloseEvent) {
        onPlayerDisconnect(event.playerUniqueId)
    }

    /** Pass through any registered plugin channels by the client. */
    @EventHandler
    public fun onChannelRegistered(event: PlayerRegisterChannelEvent) {
        onChannelRegistered(event.player.noxesiumPlayer, event.channel)
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
        super.isConnected(player) && !(player as PaperNoxesiumServerPlayer).isConnected

    override fun runDelayed(runnable: Runnable) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(NoxesiumPaper.plugin, runnable)
    }

    override fun completeHandshake(player: NoxesiumServerPlayer): Boolean = if (super.completeHandshake(player)) {
        // Emit events for other systems to hook into, only emit the register event if not transferred.
        if (!player.isTransferred) {
            Bukkit
                .getPluginManager()
                .callEvent(NoxesiumPlayerRegisteredEvent(player))
        }

        (player as PaperNoxesiumServerPlayer).player?.bukkitEntity?.also {
            Bukkit
                .getPluginManager()
                .callEvent(NoxesiumPlayerAddedToWorldEvent(it, player))
        }

        // Store the player's data externally after handshake completion
        storeData(player)
        true
    } else {
        false
    }

    override fun onPlayerDisconnect(uuid: UUID) {
        val player = NoxesiumPlayerManager.getInstance().getPlayer(uuid)
        super.onPlayerDisconnect(uuid)

        // Delete any stored data for this player
        removeStoredData(uuid)

        if (player != null) {
            // Emit an event for other systems to hook into on unregistration
            Bukkit
                .getPluginManager()
                .callEvent(NoxesiumPlayerUnregisteredEvent(player))
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
     * Indicates that stored data for [playerId] should be deleted.
     */
    protected open fun removeStoredData(playerId: UUID) {
    }
}
