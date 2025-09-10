package com.noxcrew.noxesium.sync.filesystem;

import com.noxcrew.noxesium.api.NoxesiumApi;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Watches the contents of a given folder and handles changes.
 */
public class FileSystemWatcher implements Closeable {
    /**
     * The separator char used when communicating about the files between operating systems.
     */
    private static final char UNIVERSAL_SEPARTOR_CHAR = '/';

    /**
     * All files above 64MB are ignored.
     */
    private static final long MAX_FILE_SIZE = 64000000;

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
                    StandardWatchEventKinds.ENTRY_DELETE
            );

            // Read the gitignore file to find anything that should not be synced.
            gitIgnored.add(".git");
            // TODO implement fully!

            // Create new sub-watchers for all folders
            Files.list(folder)
                    .filter(Files::isDirectory)
                    .forEach(directory -> {
                        var fileName = directory.getFileName().toString();
                        if (gitIgnored.contains(fileName)) return;
                        directories.put(fileName, new FileSystemWatcher(directory, path + UNIVERSAL_SEPARTOR_CHAR + directory.getFileSystem(), parent));
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
    public void compileContents(Map<String, Long> result) {
        try {
            Files.list(folder)
                    .filter(it -> !Files.isDirectory(it))
                    .filter(it -> getSize(it) <= MAX_FILE_SIZE)
                    .forEach(directory -> {
                        try {
                            result.put(path + UNIVERSAL_SEPARTOR_CHAR + directory.getFileName().toString(), Files.getLastModifiedTime(directory, LinkOption.NOFOLLOW_LINKS).toMillis());
                        } catch (Exception x) {
                            NoxesiumApi.getLogger().error("Failed to determine last modified time of {}", directory, x);
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
                    var filePath = path + UNIVERSAL_SEPARTOR_CHAR + fileName;
                    var isDirectory = Files.isDirectory(file);

                    // Ignore any files being changed that exceed the size limit!
                    if (!isDirectory && getSize(file) > MAX_FILE_SIZE) continue;

                    if (Objects.equals(event.kind(), StandardWatchEventKinds.ENTRY_CREATE)) {
                        if (isDirectory) {
                            // Ignore .gitignored files!
                            if (gitIgnored.contains(fileName)) continue;
                            var oldWatcher = directories.put(fileName, new FileSystemWatcher(file, filePath, parent));
                            if (oldWatcher != null) {
                                oldWatcher.close();
                            }
                        } else {
                            parent.handleAddition(filePath, Files.readAllBytes(file));
                        }
                    } else if (Objects.equals(event.kind(), StandardWatchEventKinds.ENTRY_MODIFY)) {
                        if (!isDirectory) {
                            // TODO Check if last modified changed, only then we trigger a change!
                            parent.handleChange(filePath, Files.readAllBytes(file));
                        }
                    } else if (Objects.equals(event.kind(), StandardWatchEventKinds.ENTRY_DELETE)) {
                        if (isDirectory) {
                            var watcher = directories.remove(fileName);
                            if (watcher != null) {
                                watcher.close();
                            }
                        } else {
                            parent.handleRemoval(filePath);
                        }
                    }
                }
            }
        } catch (Exception x) {
            NoxesiumApi.getLogger().error("Failed to poll events for file in folder {}", folder.toAbsolutePath(), x);
        }
        directories.values().forEach(FileSystemWatcher::poll);
    }

    @Override
    public void close() {
        // Trigger deletion for everything inside this folder!
        try {
            Files.list(folder)
                    .filter(it -> !Files.isDirectory(it))
                    .filter(it -> getSize(it) <= MAX_FILE_SIZE)
                    .forEach(directory -> {
                        try {
                            parent.handleRemoval(path + UNIVERSAL_SEPARTOR_CHAR + directory.getFileName().toString());
                        } catch (Exception x) {
                            NoxesiumApi.getLogger().error("Failed to determine last modified time of {}", directory, x);
                        }
                    });
        } catch (Exception x) {
            NoxesiumApi.getLogger().error("Failed to compile file descriptions", x);
        }

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
}
