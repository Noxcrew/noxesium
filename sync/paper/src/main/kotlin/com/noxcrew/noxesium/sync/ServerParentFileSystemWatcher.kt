package com.noxcrew.noxesium.sync

import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import com.noxcrew.noxesium.paper.network.PaperServerPlayer
import com.noxcrew.noxesium.sync.filesystem.ParentFileSystemWatcher
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundEstablishSyncPacket
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundSyncFilePacket
import com.noxcrew.noxesium.sync.network.serverbound.ServerboundSyncFilePacket
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.nio.file.Path

/**
 * Implements the server-side of the file system watcher.
 */
public class ServerParentFileSystemWatcher(
    private val folderId: String,
    folder: Path,
) : ParentFileSystemWatcher(folder) {
    private val _watchers = mutableMapOf<NoxesiumServerPlayer, Int>()

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
    public fun remove(player: NoxesiumServerPlayer): Int? =
        _watchers.remove(player)

    /**
     * Handles a new partial file.
     */
    public fun acceptFile(packet: ServerboundSyncFilePacket) {
        println("received $packet")
    }

    /** Updates the given file at [path] for [players]. */
    private fun updateFile(players: List<NoxesiumServerPlayer>, path: String) {
        val contents = collectParts(path)
        players.forEach { player ->
            val syncId = _watchers[player] ?: return@forEach
            contents.forEach { content ->
                player.sendPacket(ClientboundSyncFilePacket(syncId, content))
            }
        }
    }
}