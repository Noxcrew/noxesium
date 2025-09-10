package com.noxcrew.noxesium.sync.filesystem

import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import com.noxcrew.noxesium.paper.network.PaperServerPlayer
import com.noxcrew.noxesium.sync.event.NoxesiumSyncCompletedEvent
import com.noxcrew.noxesium.sync.network.SyncedPart
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundEstablishSyncPacket
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundSyncFilePacket
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import java.nio.file.Path

/**
 * Implements the server-side of the file system watcher.
 */
public class ServerParentFileSystemWatcher(
    private val folderId: String,
    folder: Path,
) : ParentFileSystemWatcher(folder) {
    private val _watchers = mutableMapOf<NoxesiumServerPlayer, Int>()
    private var syncCompleted = false
    private var eventCooldown = 0

    /** All players watching this file system. */
    public val watchers: Map<NoxesiumServerPlayer, Int>
        get() = _watchers

    /** Handles the initialization of this file system for [player] based on them knowing about [files]. */
    public fun initialize(player: NoxesiumServerPlayer, syncId: Int, files: Map<String, Long>): Int? {
        // Determine which files are present on the server-side
        val result = mutableMapOf<String, Long>()
        parentWatcher.compileContents(result)

        // Mark down that this user has started watching
        val oldId = _watchers.put(player, syncId)

        // Inform the client about any files the server is missing or that are not updated
        val requestedFiles = files.entries.filter { it.key !in result || it.value > result.getOrDefault(it.key, 0) }.map { it.key }
        val filesToSend = result.keys.minus(files.keys)
        player.sendPacket(ClientboundEstablishSyncPacket(syncId, requestedFiles))
        filesToSend.forEach {
            updateFile(listOf(player), it)
        }

        // Inform the player that they've started synchronizing
        (player as? PaperServerPlayer)?.player?.bukkitEntity?.sendMessage(
            Component.text(
                "You are now synchronizing $folderId",
                NamedTextColor.AQUA,
            ),
        )
        return oldId
    }

    /** Removes [player] as a watcher of this file system. */
    public fun remove(player: NoxesiumServerPlayer): Int? = _watchers.remove(player)

    /** Updates the given file at [path] for [players]. */
    private fun updateFile(players: Collection<NoxesiumServerPlayer>, path: String) {
        val contents = collectParts(path)
        players.forEach { player ->
            val syncId = _watchers[player] ?: return@forEach
            contents.forEach { content ->
                player.sendPacket(ClientboundSyncFilePacket(syncId, content))
            }
        }
    }

    override fun poll() {
        super.poll()

        // If a sync was completed in the last tick, emit an event! Avoid emitting these
        // events too frequently if the user is making frequent edits.
        if (eventCooldown > 0) {
            eventCooldown--
            return
        }

        // Ignore if no synchronization was completed
        if (!syncCompleted) return

        // Emit an event to hook into for hot-reloading based on the sync update, and prevent
        // another event from emitting for 4 tick loops (we update this every 4 ticks so this is
        // 16 ticks total minimum between events)
        Bukkit
            .getPluginManager()
            .callEvent(NoxesiumSyncCompletedEvent(folderId))
        eventCooldown = 4
        syncCompleted = false
    }

    override fun updateForAll(path: String) {
        updateFile(watchers.keys, path)
    }

    override fun handleRemoval(path: String) {
        super.handleRemoval(path)
        watchers.forEach { (player, syncId) ->
            player.sendPacket(
                ClientboundSyncFilePacket(
                    syncId,
                    // An part with 0 total means a deletion of the file!
                    SyncedPart(
                        path, 0, 0, ByteArray(0),
                    ),
                ),
            )
        }
    }

    override fun onFileUpdated() {
        super.onFileUpdated()
        syncCompleted = true
    }
}
