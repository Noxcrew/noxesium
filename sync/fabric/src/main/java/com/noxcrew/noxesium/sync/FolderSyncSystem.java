package com.noxcrew.noxesium.sync;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.sync.menu.NoxesiumFolderSyncScreen;
import com.noxcrew.noxesium.sync.network.SyncPackets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Adds the folder syncing system.
 */
public class FolderSyncSystem extends NoxesiumFeature {
    private final Map<String, Path> activeFolders = new HashMap<>();

    public FolderSyncSystem() {
        SyncPackets.CLIENTBOUND_REQUEST_SYNC.addListener(this, (reference, packet, ignored) -> {
            if (!isRegistered()) return;
            reference.startSync(packet.id());
        });
    }

    @Override
    public void onUnregister() {
        super.onUnregister();
        activeFolders.clear();
    }

    /**
     * Returns all synced folders on the current server that are active.
     */
    public Map<String, Path> getCurrentSyncedFolders() {
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
        var config = NoxesiumMod.getInstance().getConfig();
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
        return nioPath;
    }

    /**
     * Activates the given folder.
     */
    public void activateFolder(String folderId, Path location) {
        activeFolders.put(folderId, location);
    }

    /**
     * Starts the sync protocol for the given folder id.
     */
    private void startSync(String folderId) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;
        var serverData = connection.getServerData();
        if (serverData == null) return;
        var config = NoxesiumMod.getInstance().getConfig();
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
}
