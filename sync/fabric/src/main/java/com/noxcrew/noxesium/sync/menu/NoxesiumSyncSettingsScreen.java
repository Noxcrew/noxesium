package com.noxcrew.noxesium.sync.menu;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.sync.FolderSyncSystem;
import com.noxcrew.noxesium.sync.NoxesiumSyncConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.util.HashMap;
import java.util.Map;

/**
 * A screen for configuring which folders should be used for synchronization.
 */
public class NoxesiumSyncSettingsScreen extends Screen {
    private final Screen lastScreen;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final FolderSyncSystem folderSyncSystem;
    private final Map<String, MultiLineEditBox> editBoxes = new HashMap<>();
    private final NoxesiumSyncConfig config = NoxesiumSyncConfig.load();
    private String serverId;

    public NoxesiumSyncSettingsScreen(Screen lastScreen) {
        super(Component.translatable("noxesium.options.screen.sync"));
        this.folderSyncSystem = NoxesiumApi.getInstance().getFeatureOrNull(FolderSyncSystem.class);
        this.lastScreen = lastScreen;

        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;
        var serverData = connection.getServerData();
        if (serverData == null) return;
        this.serverId = serverData.ip;
    }

    @Override
    protected void init() {
        this.addTitle();
        this.addContents();
        this.addFooter();
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    protected void addTitle() {
        this.layout.addTitleHeader(this.title, this.font);
    }

    protected void addContents() {
        for (var key : config.syncableFolders.get(serverId).keySet()) {
            addEntry(key);
        }
    }

    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (button) -> this.onClose())
                .width(200)
                .build());
    }

    protected void addEntry(String folderId) {
        var header = new StringWidget(Component.literal(folderId), Minecraft.getInstance().font);
        var editBox = editBoxes.computeIfAbsent(folderId, (ignored) -> {
            var box = MultiLineEditBox.builder()
                    .build(Minecraft.getInstance().font, 300, 17, CommonComponents.EMPTY);
            box.setValue(config.syncableFolders.get(serverId).get(folderId));
            return box;
        });
        var button = Button.builder(Component.translatable("noxesium.screen.sync.request.browse"), result -> {
                    openBrowseMenu(folderId, editBox);
                })
                .bounds(0, 0, 50, 17)
                .build();

        var verticalLayout = this.layout.addToContents(LinearLayout.vertical().spacing(4));
        verticalLayout.addChild(header);
        var horizontalLayout = verticalLayout.addChild(LinearLayout.horizontal().spacing(4));
        horizontalLayout.addChild(editBox);
        horizontalLayout.addChild(button);
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    public void removed() {
        super.removed();

        // Save the values entered into the edit boxes when closing the menu!
        for (var entry : editBoxes.entrySet()) {
            folderSyncSystem.setFolder(entry.getKey(), entry.getValue().getValue());
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        this.minecraft.setScreen(this.lastScreen);
    }

    private void openBrowseMenu(String folderId, MultiLineEditBox editBox) {
        var path = TinyFileDialogs.tinyfd_selectFolderDialog("Select a folder for " + folderId, editBox.getValue());
        if (path != null) {
            editBox.setValue(path, true);
        }
    }
}
