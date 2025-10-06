package com.noxcrew.noxesium.sync.filesystem;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.sync.network.SyncedPart;
import net.minecraft.Util;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.Closeable;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The parent element to a tree of file system watchers.
 */
public abstract class ParentFileSystemWatcher implements Closeable {
    /**
     * The upper limit for the size of a single packet.
     * Any files that exceed this size are split up.
     * <p>
     * Vanilla normally allows up to 8MB but specifically
     * custom payloads are limited at 32kb.
     */
    public static final int MAX_PACKET_SIZE = 31767;

    /**
     * The area checked for a \0 byte to determine if a file
     * is a binary file.
     *
     * <a href="https://stackoverflow.com/questions/76457826/how-does-text-auto-work-how-does-git-determine-if-something-is-a-text-file">Relevant</a>
     */
    public static final int BINARY_CHECK_AREA = 8000;

    @NotNull
    private final WatchService watchService;

    @NotNull
    protected final FileSystemWatcher parentWatcher;

    @NotNull
    private final Map<Pair<Integer, String>, Set<SyncedPart>> partialFiles = new HashMap<>();

    @NotNull
    private final Map<String, Long> lastEditTimes = new HashMap<>();

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
     * Marks a file as present with a last modified time.
     */
    public void markPresent(String path, long lastModifiedTime) {
        lastEditTimes.put(path, lastModifiedTime);
    }

    /**
     * Handles the addition or change of the file at the given path.
     */
    public void handleModify(String path) {
        try {
            var filePath = parentWatcher.getFolder().resolve(path);
            var lastEditTime = Files.getLastModifiedTime(filePath, LinkOption.NOFOLLOW_LINKS)
                    .toMillis();

            // Ignore changes if they do not exceed the last known edit time
            if (lastEditTimes.getOrDefault(path, 0L) >= lastEditTime) return;
            lastEditTimes.put(path, lastEditTime);

            // Update this file for everyone
            updateForAll(path);
        } catch (Exception e) {
            NoxesiumApi.getLogger().error("Failed to handle file change of {}", path, e);
        }
    }

    /**
     * Updates the given file for all.
     */
    protected abstract void updateForAll(String path);

    /**
     * Handles the removal of the file at the given path.
     */
    public void handleRemoval(String path) {
        lastEditTimes.remove(path);
    }

    /**
     * Collects the file at the given [path] into parts.
     */
    public List<SyncedPart> collectParts(String path) {
        var file = parentWatcher.getFolder().resolve(path);
        try {
            if (Files.exists(file)) {
                var allBytes = Files.readAllBytes(file);
                if (Util.getPlatform() == Util.OS.WINDOWS) {
                    var checkArea = Math.min(allBytes.length, BINARY_CHECK_AREA);
                    var isBinary = false;
                    for (var index = 0; index < checkArea; index++) {
                        if (allBytes[index] == 0) {
                            isBinary = true;
                            break;
                        }
                    }
                    if (!isBinary) {
                        // If this file is not a binary file (so a text file) we have
                        // to detect any CRLF line endings and filter these out.
                        var encoding = UniversalDetector.detectCharset(file);
                        return split(
                                path,
                                new String(allBytes, encoding)
                                        .replace("\r\n", "\n")
                                        .getBytes(encoding));
                    }
                }
                return split(path, allBytes);
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
     * Handles a new partial file part.
     */
    public void acceptFile(int syncId, SyncedPart part) {
        // A total of 0 means the file is being deleted!
        if (part.total() == 0) {
            var file = parentWatcher.getFolder().resolve(part.path());
            try {
                Files.deleteIfExists(file);
                onFileUpdated();
            } catch (Exception e) {
                NoxesiumApi.getLogger().error("Failed to delete {}", part.path(), e);
            }
            return;
        }

        var pair = Pair.of(syncId, part.path());
        var partials = partialFiles.computeIfAbsent(pair, (it) -> new HashSet<>());
        partials.add(part);

        // If we've received all parts, save this file!
        if (partials.size() >= part.total()) {
            partialFiles.remove(pair);

            // Combine all partials into a combined array
            byte[][] parts = new byte[part.total()][];
            int size = 0;
            for (var partial : partials) {
                var array = partial.content();
                size += array.length;
                parts[partial.part()] = array;
            }
            byte[] combined = new byte[size];
            var lastIndex = 0;
            for (var index = 0; index < part.total(); index++) {
                var partial = parts[index];
                System.arraycopy(partial, 0, combined, lastIndex, partial.length);
                lastIndex += partial.length;
            }

            // Write the file to the file system
            var file = parentWatcher.getFolder().resolve(part.path());
            try {
                // Mark down the soonest time that we accept changes from!
                lastEditTimes.put(part.path(), System.currentTimeMillis() + 500);
                Files.write(file, combined);
                onFileUpdated();
            } catch (Exception e) {
                NoxesiumApi.getLogger().error("Failed to write contents of {}", part.path(), e);
            }
        }
    }

    /**
     * Hook called when a file is updated in this folder.
     */
    protected void onFileUpdated() {
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
