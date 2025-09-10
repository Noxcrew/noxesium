package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.NoxesiumApi
import com.noxcrew.noxesium.api.network.handshake.HandshakePackets
import com.noxcrew.noxesium.api.network.handshake.HandshakeState
import com.noxcrew.noxesium.api.network.handshake.NoxesiumServerHandshaker
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeTransferredPacket
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import com.noxcrew.noxesium.api.player.SerializedNoxesiumServerPlayer
import com.noxcrew.noxesium.paper.NoxesiumPaper
import com.noxcrew.noxesium.paper.api.event.NoxesiumPlayerRegisteredEvent
import com.noxcrew.noxesium.paper.api.event.NoxesiumPlayerUnregisteredEvent
import com.noxcrew.noxesium.paper.feature.noxesiumPlayer
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
public open class PaperNoxesiumServerHandshaker : NoxesiumServerHandshaker(), Listener {
    override fun register() {
        super.register()
        Bukkit.getPluginManager().registerEvents(this, NoxesiumPaper.plugin)

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
            reference.handleHandshake(PaperServerPlayer(serverPlayer, getStoredData(playerId)), packet!!)
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
     * external database. If this data is present, instantly complete the handshaking process
     * for the player.
     */
    @EventHandler
    public fun onPlayerJoin(event: PlayerJoinEvent) {
        getStoredData(event.player.uniqueId)?.also { storedData ->
            val bukkitPlayer = event.player as CraftPlayer
            val serverPlayer = bukkitPlayer.handle
            val player = PaperServerPlayer(serverPlayer, storedData)

            // Register the player instance directly
            NoxesiumPlayerManager.getInstance().registerPlayer(player.uniqueId, player)

            // Inform the client that it has been transferred to a different server
            player.sendPacket(ClientboundHandshakeTransferredPacket())

            // Re-send the player all registries, we do track the indices across stored data
            // but the registry contents may not be the same between servers so we re-sync
            // for safety. There is an option in the future to add a partial syncing protocol
            // that only syncs changes, but for now we redo it all.
            synchronizeRegistries(player.uniqueId)

            // Emit an event to hook into for configuring the player
            Bukkit
                .getPluginManager()
                .callEvent(NoxesiumPlayerRegisteredEvent(bukkitPlayer, player))
        }
    }

    @EventHandler
    public fun onPlayerQuit(event: PlayerQuitEvent) {
        onPlayerDisconnect(event.player.uniqueId)
    }

    @EventHandler
    public fun onChannelRegistered(event: PlayerRegisterChannelEvent) {
        onChannelRegistered(event.player.noxesiumPlayer, event.channel)
    }

    override fun isConnected(player: NoxesiumServerPlayer): Boolean =
        super.isConnected(player) && !(player as PaperServerPlayer).player.hasDisconnected()

    override fun runDelayed(runnable: Runnable) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(NoxesiumPaper.plugin, runnable)
    }

    override fun completeHandshake(player: NoxesiumServerPlayer): Boolean = if (super.completeHandshake(player)) {
        // Emit an event for other systems to hook into
        Bukkit
            .getPluginManager()
            .callEvent(NoxesiumPlayerRegisteredEvent((player as PaperServerPlayer).player.bukkitEntity, player))

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
            // Emit an event for other systems to hook into on unregistration
            Bukkit
                .getPluginManager()
                .callEvent(NoxesiumPlayerUnregisteredEvent((player as PaperServerPlayer).player.bukkitEntity, player))
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
}
