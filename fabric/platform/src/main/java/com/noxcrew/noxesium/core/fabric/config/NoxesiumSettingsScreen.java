package com.noxcrew.noxesium.core.fabric.config;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.core.fabric.feature.sync.FolderSyncSystem;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

/**
 * The custom settings screen used by Noxesium which opens when clicking on Noxesium in Mod Menu or by pressing F3+W.
 */
public class NoxesiumSettingsScreen extends OptionsSubScreen {

    public NoxesiumSettingsScreen(Screen screen) {
        super(screen, Minecraft.getInstance().options, Component.translatable("noxesium.options.screen.noxesium"));
    }

    @Override
    protected void addOptions() {
        this.list.addSmall(
                NoxesiumOptions.fpsOverlay(),
                NoxesiumOptions.gameTimeOverlay(),
                NoxesiumOptions.playerGlowingKeybinds());
        this.list.addSmall(
                NoxesiumOptions.dumpIncomingPackets(),
                NoxesiumOptions.dumpOutgoingPackets(),
                NoxesiumOptions.qibSystemDebugVisuals(),
                NoxesiumOptions.debugScoreboardTeams(),
                NoxesiumOptions.extendedPacketLogging(),
                NoxesiumOptions.showCullingBoxes());

        // Try to add the sync settings sub-screen
        var folderSyncSystem = NoxesiumApi.getInstance().getFeatureOrNull(FolderSyncSystem.class);
        if (folderSyncSystem == null) return;
        if (folderSyncSystem.getCurrentSyncedFolders().isEmpty()) return;
        this.list.addSmall(List.of(Button.builder(Component.translatable("noxesium.options.sync.folders"), (button) -> {
                    Minecraft.getInstance().setScreen(new NoxesiumSyncSettingsScreen(this));
                })
                .bounds(0, 0, 150, 20)
                .build()));
    }
}
