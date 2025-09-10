package com.noxcrew.noxesium.sync.menu;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.sync.filesystem.FolderSyncSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

/**
 * A screen where you can select which folder to save to.
 */
public class NoxesiumFolderSelectScreen extends Screen {

    private final LinearLayout layout;
    private final String folderId;
    private final StringWidget headerText;
    private final MultiLineEditBox editBox;
    private final Button browseButton, confirmButton;

    public NoxesiumFolderSelectScreen(String folderId) {
        super(Component.translatable("noxesium.screen.sync.select.header"));
        this.layout = LinearLayout.vertical().spacing(8);
        this.folderId = folderId;
        this.headerText = new StringWidget(
                Component.translatable("noxesium.screen.sync.select.header", folderId), Minecraft.getInstance().font);
        this.editBox = MultiLineEditBox.builder().build(Minecraft.getInstance().font, 300, 17, CommonComponents.EMPTY);
        this.browseButton = Button.builder(Component.translatable("noxesium.screen.sync.request.browse"), result -> {
                    openBrowseMenu();
                })
                .bounds(0, 0, 50, 17)
                .build();
        this.confirmButton = Button.builder(Component.translatable("noxesium.screen.sync.request.confirm"), result -> {
                    NoxesiumApi.getInstance()
                            .getFeatureOptional(FolderSyncSystem.class)
                            .ifPresent(folderSyncSystem -> {
                                var path = folderSyncSystem.setFolder(folderId, editBox.getValue());
                                if (path != null) {
                                    folderSyncSystem.activateFolder(folderId, path);
                                }
                            });
                    Minecraft.getInstance().setScreen(null);
                })
                .bounds(0, 0, 100, 20)
                .build();
        this.confirmButton.active = false;
    }

    @Override
    protected void init() {
        this.layout.defaultCellSetting().alignHorizontallyCenter();
        this.layout.addChild(this.headerText);
        LinearLayout linearlayout =
                this.layout.addChild(LinearLayout.horizontal().spacing(4));
        linearlayout.defaultCellSetting().paddingTop(16);
        linearlayout.addChild(this.editBox);
        linearlayout.addChild(this.browseButton);
        this.layout.addChild(this.confirmButton);
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    /**
     * Opens a menu to browse for a folder to synchronize.
     */
    private void openBrowseMenu() {
        var path = TinyFileDialogs.tinyfd_selectFolderDialog("Select a folder for " + folderId, editBox.getValue());
        if (path != null) {
            this.editBox.setValue(path, true);
            if (!path.isBlank()) {
                this.confirmButton.active = true;
            }
        }
    }
}
