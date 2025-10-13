package com.noxcrew.noxesium.sync.filesystem;

import com.noxcrew.noxesium.api.NoxesiumApi;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

/**
 * Watches the contents of a given folder and handles changes.
 */
public class FileSystemWatcher implements Closeable {
    /**
     * The separator char used when communicating about the files between operating systems.
     */
    public static final char UNIVERSAL_SEPARTOR_CHAR = '/';

    /**
     * All files above 64MB are ignored.
     */
    public static final long MAX_FILE_SIZE = 64000000;

    /**
     * The inaccuracy accepted in timestamps between the client and server. Only used when comparing
     * between sides as we assume there is a possible difference in synchronization.
     */
    public static final long IGNORED_MODIFY_OFFSET = 1000;

    @NotNull
    private final Path folder;

    @NotNull
    private final ParentFileSystemWatcher parent;

    @NotNull
    private final String path;

    @NotNull
    private final WatchKey watchKey;

    @NotNull
    private final Map<String, FileSystemWatcher> directories = new ConcurrentHashMap<>();

    @NotNull
    private final Set<String> gitIgnored = new HashSet<>();

    public FileSystemWatcher(@NotNull Path folder, @NotNull String path, @NotNull ParentFileSystemWatcher parent) {
        try {
            this.folder = folder;
            this.path = path;
            this.parent = parent;
            this.watchKey = folder.register(
                    parent.getWatchService(),
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);

            // Read the gitignore file to find anything that should not be synced.
            gitIgnored.add(".git");
            // TODO implement .gitignore support!

            Files.list(folder).forEach(file -> {
                var fileName = file.getFileName().toString();

                // Create new sub-watchers for all folders
                if (Files.isDirectory(file)) {
                    if (gitIgnored.contains(fileName)) return;
                    directories.put(fileName, new FileSystemWatcher(file, getRelative(fileName), parent));
                } else {
                    // Mark down for all regular files when they were last edited so we can
                    // ignore any modifications that do not exceed that time.
                    try {
                        parent.markPresent(
                                getRelative(file.getFileName().toString()),
                                Files.getLastModifiedTime(file, LinkOption.NOFOLLOW_LINKS)
                                        .toMillis());
                    } catch (Exception x) {
                        NoxesiumApi.getLogger().error("Failed to determine last modified time of {}", file, x);
                    }
                }
            });
        } catch (Exception x) {
            throw new RuntimeException("Failed to set up file system watcher", x);
        }
    }

    /**
     * Returns the path of this folder.
     */
    @NotNull
    public Path getFolder() {
        return folder;
    }

    /**
     * Adds contents of this folder to the given resulting map.
     */
    public void compileContents(Map<String[], Map<String, Long>> result) {
        try {
            var stem = path.isBlank() ? new String[0] : path.split(Character.toString(UNIVERSAL_SEPARTOR_CHAR));
            Files.list(folder)
                    .filter(it -> !Files.isDirectory(it))
                    .filter(it -> getSize(it) <= MAX_FILE_SIZE)
                    .forEach(file -> {
                        try {
                            result.computeIfAbsent(stem, (it) -> new HashMap<>())
                                    .put(
                                            file.getFileName().toString(),
                                            Files.getLastModifiedTime(file, LinkOption.NOFOLLOW_LINKS)
                                                    .toMillis());
                        } catch (Exception x) {
                            NoxesiumApi.getLogger().error("Failed to determine last modified time of {}", file, x);
                        }
                    });
        } catch (Exception x) {
            NoxesiumApi.getLogger().error("Failed to compile file descriptions", x);
        }
        directories.values().forEach(it -> it.compileContents(result));
    }

    /**
     * Polls this service for updates.
     */
    public void poll() {
        try {
            var events = watchKey.pollEvents();
            if (!events.isEmpty()) {
                watchKey.reset();
                for (var event : events) {
                    var file = ((Path) watchKey.watchable()).resolve((Path) event.context());
                    var fileName = file.getFileName().toString();
                    var filePath = getRelative(fileName);
                    var isDirectory = Files.isDirectory(file);

                    if (Objects.equals(event.kind(), StandardWatchEventKinds.ENTRY_CREATE)) {
                        if (isDirectory) {
                            // Ignore .gitignored files!
                            if (gitIgnored.contains(fileName)) continue;
                            var oldWatcher = directories.put(fileName, new FileSystemWatcher(file, filePath, parent));
                            if (oldWatcher != null) {
                                oldWatcher.close();
                            }
                        } else {
                            // Ignore any files being changed that exceed the size limit!
                            if (getSize(file) > MAX_FILE_SIZE) continue;
                            parent.handleModify(filePath);
                        }
                    } else if (Objects.equals(event.kind(), StandardWatchEventKinds.ENTRY_MODIFY)) {
                        if (!isDirectory) {
                            // Ignore any files being changed that exceed the size limit!
                            if (getSize(file) > MAX_FILE_SIZE) continue;
                            parent.handleModify(filePath);
                        }
                    } else if (Objects.equals(event.kind(), StandardWatchEventKinds.ENTRY_DELETE)) {
                        // Treat deletions of files that still exist as modifications!
                        if (Files.exists(file)) {
                            if (!isDirectory) {
                                // Ignore any files being changed that exceed the size limit!
                                if (getSize(file) > MAX_FILE_SIZE) continue;
                                parent.handleModify(filePath);
                            }
                            continue;
                        }

                        if (isDirectory) {
                            var watcher = directories.remove(fileName);
                            if (watcher != null) {
                                watcher.markDeleted();
                                watcher.close();
                            }
                        } else {
                            parent.handleRemoval(filePath);
                        }
                    } else {
                        NoxesiumApi.getLogger().info("Unknown event {} for file {}", event.kind(), file);
                    }
                }
            }
        } catch (Exception x) {
            NoxesiumApi.getLogger().error("Failed to poll events for file in folder {}", folder.toAbsolutePath(), x);
        }
        directories.values().forEach(FileSystemWatcher::poll);
    }

    /**
     * Marks this folder as deleted.
     */
    public void markDeleted() {
        // Trigger deletion for everything inside this folder!
        try {
            Files.list(folder)
                    .filter(it -> !Files.isDirectory(it))
                    .filter(it -> getSize(it) <= MAX_FILE_SIZE)
                    .forEach(file -> {
                        try {
                            // If this file exists, it's not gone!
                            if (Files.exists(file)) return;

                            var fileName = file.getFileName().toString();
                            var filePath = getRelative(fileName);

                            if (Files.isDirectory(file)) {
                                var watcher = directories.remove(fileName);
                                if (watcher != null) {
                                    watcher.markDeleted();
                                    watcher.close();
                                }
                            } else {
                                parent.handleRemoval(filePath);
                            }
                        } catch (Exception x) {
                            NoxesiumApi.getLogger().error("Failed to determine last modified time of {}", file, x);
                        }
                    });
        } catch (Exception x) {
            NoxesiumApi.getLogger().error("Failed to compile file descriptions", x);
        }
    }

    @Override
    public void close() {
        this.watchKey.cancel();
        this.directories.values().forEach(FileSystemWatcher::close);
    }

    /**
     * Returns the size of the given file.
     */
    private long getSize(Path file) {
        try {
            return Files.size(file);
        } catch (IOException e) {
            return MAX_FILE_SIZE + 1;
        }
    }

    /**
     * Returns the given file name as relative to this path.
     */
    private String getRelative(String fileName) {
        if (path.isBlank()) {
            return fileName;
        } else {
            return path + UNIVERSAL_SEPARTOR_CHAR + fileName;
        }
    }
}
