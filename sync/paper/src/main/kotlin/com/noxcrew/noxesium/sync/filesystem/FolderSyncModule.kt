package com.noxcrew.noxesium.sync.filesystem

import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import com.noxcrew.noxesium.paper.NoxesiumPaper
import com.noxcrew.noxesium.paper.api.event.NoxesiumPlayerUnregisteredEvent
import com.noxcrew.noxesium.paper.feature.ListeningNoxesiumFeature
import com.noxcrew.noxesium.paper.network.PaperNoxesiumServerPlayer
import com.noxcrew.noxesium.sync.network.SyncPackets
import com.noxcrew.noxesium.sync.network.serverbound.ServerboundRequestSyncPacket
import com.noxcrew.noxesium.sync.network.serverbound.ServerboundSyncFilePacket
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.exists

/**
 * Sets up the folder syncing system, this is a system that mirrors a folder
 * on the server to the client's game directory. This can be used to allow clients
 * to modify the server's configuration files without needing to receive FTP access
 * or if the server's assets are inside a Docker container and synchronized using a
 * Git repository.
 *
 * The system can run either client- or server-authoritatively. If the synced folder
 * on the client is a git repository it will be client-authoritative. Otherwise, it
 * will be server-authoritative. That is, the authoritative side is presumed correct
 * and its files are sent to the other. Even if it's server-authoritative the client
 * can still make changes, but it allows other users to also make changes at the same
 * time.
 *
 * Clients can configure the location of the folder on their PC themselves so they
 * can link it to pre-existing cloned git repositories as well.
 */
public class FolderSyncModule : ListeningNoxesiumFeature() {
    public companion object {
        /** The required permission node to perform Noxesium syncing. */
        public const val PERMISSION_NODE: String = "noxesium.sync"
    }

    private val folders = mutableMapOf<String, Path>()
    private val sessions = mutableMapOf<String, ServerParentFileSystemWatcher>()
    private val watchersById = mutableMapOf<NoxesiumServerPlayer, MutableMap<Int, ServerParentFileSystemWatcher>>()

    /** All folders that admins can enable syncing for. */
    public val syncableFolders: Map<String, Path>
        get() = folders

    init {
        // If the sync example folder exists we register it so the user can test out the system!
        val syncExample =
            NoxesiumPaper.plugin.dataFolder
                .toPath()
                .resolve("sync_example")
        if (syncExample.exists()) {
            register("example", syncExample)
        }

        // Listen to events from the client related to synchronization
        SyncPackets.SERVERBOUND_REQUEST_SYNC.addListener(this, ServerboundRequestSyncPacket::class.java) { reference, packet, playerId ->
            if (!reference.isRegistered) return@addListener
            reference.handleSyncRequest(playerId, packet)
        }
        SyncPackets.SERVERBOUND_SYNC_FILE.addListener(this, ServerboundSyncFilePacket::class.java) { reference, packet, playerId ->
            if (!reference.isRegistered) return@addListener
            reference.acceptFile(playerId, packet)
        }

        // Tick polling on all active sessions
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
            NoxesiumPaper.plugin,
            {
                if (!isRegistered) return@scheduleSyncRepeatingTask
                sessions.values.forEach { it.poll() }
            },
            4, 4,
        )
    }

    /** Handles a request from a client to start syncing a new folder. */
    private fun handleSyncRequest(playerId: UUID, packet: ServerboundRequestSyncPacket) {
        // Double-check that this player has the required permissions! This avoids clients from faking their way
        // into synchronization sessions the server didn't request.
        val player = NoxesiumPlayerManager.getInstance().getPlayer(playerId) as? PaperNoxesiumServerPlayer ?: return
        val bukkitPlayer = player.player?.bukkitEntity ?: return
        if (!bukkitPlayer.hasPermission(PERMISSION_NODE)) return
        val folder = syncableFolders[packet.id] ?: return

        // Create a new session if necessary and add the player to it
        val session = sessions.computeIfAbsent(packet.id) { ServerParentFileSystemWatcher(it, folder) }
        val oldId = session.initialize(player, packet.syncId(), packet.files())
        val watchers = watchersById.computeIfAbsent(player) { mutableMapOf() }
        oldId?.also { watchers -= it }
        watchers[packet.syncId()] = session
    }

    /** Accepts a file being synchronized with the server. */
    private fun acceptFile(playerId: UUID, packet: ServerboundSyncFilePacket) {
        val player = NoxesiumPlayerManager.getInstance().getPlayer(playerId) ?: return
        val watcher = watchersById[player]?.get(packet.syncId()) ?: return
        watcher.acceptFile(packet.syncId, packet.part)
    }

    /** Registers a new folder to be syncable. */
    public fun register(id: String, folder: Path) {
        folders[id] = folder
    }

    /**
     * End any active sessions when the player disconnects.
     */
    @EventHandler
    public fun onPlayerUnregistered(e: NoxesiumPlayerUnregisteredEvent) {
        sessions.values.removeIf { watcher ->
            // Remove this player as a watcher
            val syncId = watcher.remove(e.noxesiumPlayer)
            syncId?.also { watchersById[e.noxesiumPlayer]?.remove(it) }

            // Destroy the session if there are no watchers left to
            // save on system resources.
            if (watcher.watchers.isEmpty()) {
                watcher.close()
                true
            } else {
                false
            }
        }
    }
}
