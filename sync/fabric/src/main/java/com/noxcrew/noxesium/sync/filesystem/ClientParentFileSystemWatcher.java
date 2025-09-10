package com.noxcrew.noxesium.sync.filesystem;

import com.noxcrew.noxesium.api.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.sync.network.serverbound.ServerboundRequestSyncPacket;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.HashMap;

/**
 * Extends the file system watcher with client-exclusive behavior.
 */
public class ClientParentFileSystemWatcher extends ParentFileSystemWatcher {
    /**
     * The last unique id used by a synchronizing file system. Ids are decided on the
     * client side and tracked per player by the server.
     */
    private static int LAST_SYNCHRONIZATION_ID = 0;

    @NotNull
    private final String folderId;
    private final int syncId;

    public ClientParentFileSystemWatcher(@NotNull Path folder, @NotNull String folderId) {
        super(folder);
        this.folderId = folderId;
        this.syncId = LAST_SYNCHRONIZATION_ID++;
    }

    /**
     * Returns the synchronization id of this watcher, used by the
     * client and server to communicate about this specific instance
     * of a folder being synchronized.
     */
    public int getSynchronizationId() {
        return syncId;
    }

    /**
     * Initializes this watcher, starting by synchronizing the current file tree
     * with the server so it's known what has to be synchronized.
     */
    public void initialize() {
        // First we gather a full description of the file system on this side
        var result = new HashMap<String, Long>();
        parentWatcher.compileContents(result);

        // Send the full description to the server with a request to start receiving
        // information on any changes made to this folder.
        NoxesiumServerboundNetworking.send(new ServerboundRequestSyncPacket(folderId, syncId, result));
    }
}
