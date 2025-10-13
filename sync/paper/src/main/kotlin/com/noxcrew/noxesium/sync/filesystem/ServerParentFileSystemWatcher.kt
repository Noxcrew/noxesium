package com.noxcrew.noxesium.sync.filesystem

import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import com.noxcrew.noxesium.paper.NoxesiumPaper
import com.noxcrew.noxesium.paper.network.PaperNoxesiumServerPlayer
import com.noxcrew.noxesium.sync.event.NoxesiumSyncCompletedEvent
import com.noxcrew.noxesium.sync.network.SyncedPart
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundRequestFilePacket
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundSyncFilePacket
import com.noxcrew.noxesium.sync.network.serverbound.ServerboundFileSystemPacket
import io.netty.buffer.ByteBufUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import java.nio.file.Path

/**
 * Implements the server-side of the file system watcher.
 */
public class ServerParentFileSystemWatcher(
    /** The id of this folder. */
    public val folderId: String,
    folder: Path,
) : ParentFileSystemWatcher(folder) {
    private val _watchers = mutableMapOf<NoxesiumServerPlayer, Int>()
    private val pendingFileSystem = mutableMapOf<NoxesiumServerPlayer, MutableList<ServerboundFileSystemPacket>>()
    private var syncCompleted = false
    private var eventCooldown = 0

    /** All players watching this file system. */
    public val watchers: Map<NoxesiumServerPlayer, Int>
        get() = _watchers

    /** Submits a part of the file system of [player]. */
    public fun submitFileSystem(packet: ServerboundFileSystemPacket, player: NoxesiumServerPlayer) {
        // The list of pending packets in the file system
        val pendingList = pendingFileSystem.computeIfAbsent(player) { mutableListOf() }
        pendingList += packet
        if (pendingList.size < packet.total) return

        // Clear the pending list and then start a new async task
        pendingFileSystem -= player
        Bukkit.getScheduler().runTaskAsynchronously(
            NoxesiumPaper.plugin,
            Runnable {
                // Determine which files are present on the server-side
                val result = mutableMapOf<Array<String>, Map<String, Long>>()
                parentWatcher.compileContents(result)

                // Flatten the results into bare paths
                val flattenedResult = mutableMapOf<String, Long>()
                for (path in result.keys) {
                    for ((fileName, time) in result.getValue(path)) {
                        flattenedResult[
                            if (path.isEmpty()) {
                                fileName
                            } else {
                                path.joinToString(FileSystemWatcher.UNIVERSAL_SEPARTOR_CHAR.toString()) +
                                    FileSystemWatcher.UNIVERSAL_SEPARTOR_CHAR + fileName
                            },
                        ] = time
                    }
                }

                // Inform the client about any files the server is missing or that are not updated
                val files = mutableMapOf<String, Long>()
                for (partial in pendingList) {
                    for ((path, contents) in partial.contents) {
                        for ((fileName, time) in contents) {
                            files[
                                if (path.isEmpty()) {
                                    fileName
                                } else {
                                    path.joinToString(FileSystemWatcher.UNIVERSAL_SEPARTOR_CHAR.toString()) +
                                        FileSystemWatcher.UNIVERSAL_SEPARTOR_CHAR + fileName
                                },
                            ] = time
                        }
                    }
                }

                // Compare the files on both sides and send off requests to sort out these differences
                val requestedFiles =
                    files.entries
                        .filter {
                            it.key !in flattenedResult ||
                                it.value > (
                                    flattenedResult.getOrDefault(
                                        it.key,
                                        0,
                                    ) + FileSystemWatcher.IGNORED_MODIFY_OFFSET
                                )
                        }.map { it.key }
                val filesToSend = flattenedResult.keys.minus(files.keys)

                if (requestedFiles.isNotEmpty()) {
                    var pendingBytes = 0L
                    val pendingFiles = mutableListOf<String>()
                    for (file in requestedFiles) {
                        pendingFiles += file
                        pendingBytes += ByteBufUtil.utf8MaxBytes(file)

                        if (pendingBytes >= FileSystemWatcher.MAX_FILE_SIZE) {
                            player.sendPacket(ClientboundRequestFilePacket(packet.syncId, pendingFiles))
                            pendingFiles.clear()
                            pendingBytes = 0
                        }
                    }
                    if (pendingFiles.isNotEmpty()) {
                        player.sendPacket(ClientboundRequestFilePacket(packet.syncId, pendingFiles))
                    }
                }
                filesToSend.forEach {
                    updateFile(listOf(player), it)
                }
            },
        )
    }

    /** Handles the initialization of this file system for [player]. */
    public fun initialize(player: NoxesiumServerPlayer, syncId: Int): Int? {
        // Mark down that this user has started watching
        val oldId = _watchers.put(player, syncId)

        // Inform the player that they've started synchronizing
        (player as? PaperNoxesiumServerPlayer)?.player?.bukkitEntity?.sendMessage(
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

    override fun handleRemoval(path: String): Boolean {
        if (super.handleRemoval(path)) {
            watchers.forEach { (player, syncId) ->
                player.sendPacket(
                    ClientboundSyncFilePacket(
                        syncId,
                        // An part with 0 total means a deletion of the file!
                        SyncedPart(
                            path, 0, 0, 0, ByteArray(0),
                        ),
                    ),
                )
            }
            return true
        }
        return false
    }

    override fun onFileUpdated() {
        super.onFileUpdated()
        syncCompleted = true
    }
}
