package com.noxcrew.noxesium.sync.filesystem;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.sync.NoxesiumSyncConfig;
import com.noxcrew.noxesium.sync.menu.NoxesiumFolderSyncScreen;
import com.noxcrew.noxesium.sync.network.SyncPackets;
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundEstablishSyncPacket;
import com.noxcrew.noxesium.sync.network.clientbound.ClientboundSyncFilePacket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Adds the folder syncing system.
 */
public class FolderSyncSystem extends NoxesiumFeature {
    private final Map<String, ClientParentFileSystemWatcher> activeFolders = new HashMap<>();
    private final Map<Integer, ClientParentFileSystemWatcher> watchersById = new HashMap<>();

    public FolderSyncSystem() {
        SyncPackets.CLIENTBOUND_REQUEST_SYNC.addListener(this, (reference, packet, ignored) -> {
            if (!reference.isRegistered()) return;

            // Start a sync when the server requests it. The client has to confirm it first for this specific IP
            // so random servers cannot start a request unless the client has already allowed it.
            reference.startSync(packet.id());
        });
        SyncPackets.CLIENTBOUND_ESTABLISH_SYNC.addListener(this, (reference, packet, ignored) -> {
            if (!reference.isRegistered()) return;
            reference.establishSync(packet);
        });
        SyncPackets.CLIENTBOUND_SYNC_FILE.addListener(this, (reference, packet, ignored) -> {
            if (!reference.isRegistered()) return;
            reference.acceptFile(packet);
        });

        ClientTickEvents.END_CLIENT_TICK.register((ignored) -> {
            // Poll changes on all registered watcher on tick end
            if (!isRegistered()) return;
            activeFolders.values().forEach(ParentFileSystemWatcher::poll);
        });
    }

    @Override
    public void onTransfer() {
        super.onTransfer();
        activeFolders.clear();
    }

    /**
     * Returns all synced folders on the current server that are active.
     */
    public Map<String, ClientParentFileSystemWatcher> getCurrentSyncedFolders() {
        return activeFolders;
    }

    /***
     * Sets the folder path for the given id.
     */
    @Nullable
    public Path setFolder(String folderId, String path) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) return null;
        var serverData = connection.getServerData();
        if (serverData == null) return null;
        var config = NoxesiumSyncConfig.load();
        var storedFolders = config.syncableFolders.computeIfAbsent(serverData.ip, (it) -> new HashMap<>());
        var nioPath = Path.of(path);

        // Ensure the folder exists!
        try {
            Files.createDirectories(nioPath);
        } catch (Exception exception) {
            NoxesiumApi.getLogger().warn("Failed to create folder for synced folder {}", folderId, exception);
            return null;
        }

        storedFolders.put(folderId, path);

        // If the folder is already being watched, create a new watcher!
        if (activeFolders.containsKey(folderId)) {
            var newWatcher = new ClientParentFileSystemWatcher(nioPath, folderId);
            var oldWatcher = activeFolders.put(folderId, newWatcher);
            watchersById.remove(oldWatcher.getSynchronizationId());
            oldWatcher.close();

            // Re-initialize the new location of the watcher!
            watchersById.put(newWatcher.getSynchronizationId(), newWatcher);
            newWatcher.initialize();
        }
        config.save();
        return nioPath;
    }

    /**
     * Activates the given folder.
     */
    public void activateFolder(String folderId, Path location) {
        var newWatcher = new ClientParentFileSystemWatcher(location, folderId);
        var currentWatcher = activeFolders.put(folderId, newWatcher);
        if (currentWatcher != null) {
            watchersById.remove(currentWatcher.getSynchronizationId());
            currentWatcher.close();
        }

        // Start synchronizing with the server on the start of the folder
        watchersById.put(newWatcher.getSynchronizationId(), newWatcher);
        newWatcher.initialize();
    }

    /**
     * Starts the sync protocol for the given folder id.
     */
    private void startSync(String folderId) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;
        var serverData = connection.getServerData();
        if (serverData == null) return;
        var config = NoxesiumSyncConfig.load();
        var hadPreviousAttempt = false;
        var storedFolders = config.syncableFolders.computeIfAbsent(serverData.ip, (it) -> new HashMap<>());
        if (storedFolders.containsKey(folderId)) {
            var previousPath = Path.of(storedFolders.get(folderId));
            if (previousPath.toFile().exists()) {
                activateFolder(folderId, previousPath);
                return;
            } else {
                hadPreviousAttempt = true;
            }
        }

        // Build the text to show in the menu
        var text = Component.translatable("noxesium.screen.sync.request.header", folderId);
        if (hadPreviousAttempt) {
            text = text.append(CommonComponents.SPACE);
            text = text.append(
                    Component.translatable("noxesium.screen.sync.request.previous", storedFolders.get(folderId)));
        }
        text = text.append(CommonComponents.SPACE);
        text = text.append(Component.translatable("noxesium.screen.sync.request.footer"));
        Minecraft.getInstance().setScreen(new NoxesiumFolderSyncScreen(text, folderId));
    }

    /**
     * Handles a synchronization being established.
     */
    private void establishSync(ClientboundEstablishSyncPacket packet) {
        var sync = watchersById.get(packet.syncId());
        if (sync == null) return;
        for (var file : packet.requestedFiles()) {
            sync.updateForAll(file);
        }
    }

    /**
     * Handles part of a file being received.
     */
    private void acceptFile(ClientboundSyncFilePacket packet) {
        var sync = watchersById.get(packet.syncId());
        if (sync == null) return;
        sync.acceptFile(packet.syncId(), packet.part());
    }
}
