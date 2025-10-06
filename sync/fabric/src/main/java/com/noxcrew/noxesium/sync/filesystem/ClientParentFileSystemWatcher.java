package com.noxcrew.noxesium.sync.filesystem;

import com.noxcrew.noxesium.api.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.sync.network.SyncedPart;
import com.noxcrew.noxesium.sync.network.serverbound.ServerboundFileSystemPacket;
import com.noxcrew.noxesium.sync.network.serverbound.ServerboundRequestSyncPacket;
import com.noxcrew.noxesium.sync.network.serverbound.ServerboundSyncFilePacket;
import io.netty.buffer.ByteBufUtil;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.network.VarLong;
import org.jetbrains.annotations.NotNull;

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
    private final AtomicBoolean pendingInitialization = new AtomicBoolean(false);

    public ClientParentFileSystemWatcher(@NotNull Path folder, @NotNull String folderId) {
        super(folder);
        this.folderId = folderId;
        this.syncId = LAST_SYNCHRONIZATION_ID++;
    }

    /**
     * Asynchronously tick, which waits for initialization to start before
     * doing file system loading.
     */
    public void tickAsync() {
        if (!pendingInitialization.compareAndSet(true, false)) return;

        // First we gather a full description of the file system on this side
        var result = new HashMap<String[], Map<String, Long>>();
        parentWatcher.compileContents(result);

        // Split up the file system into chunks that are not too big
        var currentSize = 0L;
        var partial = new HashMap<List<String>, Map<String, Long>>();
        var totalParts = 0;
        var finished = new HashSet<Map<List<String>, Map<String, Long>>>();
        for (var key : result.keySet()) {
            var list = Arrays.asList(key);
            currentSize += ByteBufUtil.utf8MaxBytes(list.toString());

            var contents = result.get(key);
            for (var entry : contents.entrySet()) {
                partial.computeIfAbsent(list, (it) -> new HashMap<>()).put(entry.getKey(), entry.getValue());
                currentSize += ByteBufUtil.utf8MaxBytes(entry.getKey());
                currentSize += VarLong.getByteSize(entry.getValue());

                // Split up the data if it's too long!
                if (currentSize >= ParentFileSystemWatcher.MAX_PACKET_SIZE) {
                    totalParts++;
                    finished.add(partial);
                    currentSize = 0;
                    currentSize += ByteBufUtil.utf8MaxBytes(list.toString());
                    partial = new HashMap<>();
                }
            }
        }

        // Add what remains!
        if (!partial.isEmpty()) {
            totalParts++;
            finished.add(partial);
        }

        // Send all parts together now that we know the total amount!
        var index = 0;
        for (var map : finished) {
            NoxesiumServerboundNetworking.send(new ServerboundFileSystemPacket(syncId, index++, totalParts, map));
        }
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
        // Inform the server we would like to start synchronizing, then queue up the file system sync!
        NoxesiumServerboundNetworking.send(new ServerboundRequestSyncPacket(folderId, syncId));
        pendingInitialization.set(true);
    }

    @Override
    protected void updateForAll(String path) {
        var parts = collectParts(path);
        for (var part : parts) {
            NoxesiumServerboundNetworking.send(new ServerboundSyncFilePacket(syncId, part));
        }
    }

    @Override
    public void handleRemoval(String path) {
        super.handleRemoval(path);
        NoxesiumServerboundNetworking.send(
                new ServerboundSyncFilePacket(syncId, new SyncedPart(path, 0, 0, new byte[0])));
    }
}
