package com.noxcrew.noxesium.sync.filesystem;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.sync.network.SyncedPart;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;

/**
 * The parent element to a tree of file system watchers.
 */
public class ParentFileSystemWatcher implements Closeable {
    /**
     * The upper limit for the size of a single packet.
     * Any files that exceed this size are split up.
     *
     * Lowered by 80k from vanilla to account for the rest
     * of the packet.
     */
    private static final int MAX_PACKET_SIZE = 8308608;

    @NotNull
    private final WatchService watchService;

    @NotNull
    protected final FileSystemWatcher parentWatcher;

    public ParentFileSystemWatcher(@NotNull Path folder) {
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            this.parentWatcher = new FileSystemWatcher(folder, "", this);
        } catch (Exception x) {
            throw new RuntimeException("Failed to create watch service", x);
        }
    }

    /**
     * Polls the parent watcher of this system tree.
     */
    public void poll() {
        parentWatcher.poll();
    }

    /**
     * Collects the file at the given [path] into parts.
     */
    public List<SyncedPart> collectParts(String path) {
        var file = parentWatcher.getFolder().resolve(path);
        try {
            if (Files.exists(file)) {
                return split(path, Files.readAllBytes(file));
            }
        } catch (Exception e) {
            NoxesiumApi.getLogger().error("Failed to read contents of {}", path, e);
        }
        return List.of();
    }

    /**
     * Splits the given content into segments.
     */
    public List<SyncedPart> split(String path, byte[] contents) {
        var size = contents.length;
        var parts = ((size - 1) / MAX_PACKET_SIZE) + 1;
        var list = new ArrayList<SyncedPart>();
        for (int index = 0; index < parts; index++) {
            var start = index * MAX_PACKET_SIZE;
            var end = Math.min(size, (index + 1) * MAX_PACKET_SIZE);
            var length = end - start;
            var array = new byte[length];
            System.arraycopy(contents, start, array, 0, length);
            list.add(new SyncedPart(path, index, parts, array));
        }
        return list;
    }

    /**
     * Handles the addition of the file at the given path.
     */
    public void handleAddition(String path, byte[] contents) {
        System.out.println("new file " + path);
    }

    /**
     * Handles the modification of the file at the given path.
     */
    public void handleChange(String path, byte[] newContents) {
        System.out.println("changed file " + path);
    }

    /**
     * Handles the removal of the file at the given path.
     */
    public void handleRemoval(String path) {
        System.out.println("deleted file " + path);

    }

    /**
     * Returns the watch service to use.
     */
    @NotNull
    public WatchService getWatchService() {
        return watchService;
    }

    @Override
    public void close() {
        parentWatcher.close();
    }
}
