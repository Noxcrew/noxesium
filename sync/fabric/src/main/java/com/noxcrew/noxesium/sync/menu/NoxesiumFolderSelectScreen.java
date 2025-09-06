package com.noxcrew.noxesium.sync.menu;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.sync.FolderSyncSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * A screen where you can select which folder to save to.
 */
public class NoxesiumFolderSelectScreen extends Screen {

    private final String folderId;
    private final Button confirmButton;

    public NoxesiumFolderSelectScreen(String folderId) {
        super(Component.translatable("noxesium.screen.sync.select.header"));
        this.folderId = folderId;
        this.confirmButton = Button.builder(CommonComponents.GUI_DONE, result -> {
                    NoxesiumApi.getInstance()
                            .getFeatureOptional(FolderSyncSystem.class)
                            .ifPresent(folderSyncSystem -> {
                                var path = folderSyncSystem.setFolder(folderId, "");
                                if (path != null) {
                                    folderSyncSystem.activateFolder(folderId, path);
                                }
                            });
                    Minecraft.getInstance().setScreen(null);
                })
                .bounds(this.width / 2 - 100, this.height / 4 + 144, 200, 20)
                .build();
        this.confirmButton.active = false;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(confirmButton);
    }
}
