package com.noxcrew.noxesium.paper.feature

import com.noxcrew.noxesium.api.feature.NoxesiumFeature
import com.noxcrew.noxesium.paper.NoxesiumPaper
import java.nio.file.Path
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
public class FolderSyncModule : NoxesiumFeature() {
    private val folders = mutableMapOf<String, Path>()

    // TODO move to api!

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
    }

    /** Registers a new folder to be syncable. */
    public fun register(id: String, folder: Path) {
        folders[id] = folder
    }
}
