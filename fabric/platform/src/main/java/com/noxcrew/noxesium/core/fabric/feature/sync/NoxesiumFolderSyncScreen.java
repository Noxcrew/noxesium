package com.noxcrew.noxesium.core.fabric.feature.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;

/**
 * A screen for accepting a server's request to synchronize a folder.
 */
public class NoxesiumFolderSyncScreen extends ConfirmScreen {

    public NoxesiumFolderSyncScreen(Component text, String folderId) {
        super(
                (value) -> {
                    if (value) {
                        Minecraft.getInstance().setScreen(new NoxesiumFolderSelectScreen(folderId));
                    } else {
                        Minecraft.getInstance().setScreen(null);
                    }
                },
                Component.empty(),
                text,
                Component.translatable("noxesium.screen.sync.request.accept"),
                Component.translatable("noxesium.screen.sync.request.deny"));
    }

    @Override
    protected void addButtons(LinearLayout linearLayout) {
        super.addButtons(linearLayout);

        // Delay the buttons by 2 seconds so people have to actually read the message
        // and cannot mindlessly skip it!
        setDelay(40);
    }
}
