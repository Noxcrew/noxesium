package com.noxcrew.noxesium.sync.mixin;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.core.fabric.config.NoxesiumSettingsScreen;
import com.noxcrew.noxesium.sync.filesystem.FolderSyncSystem;
import com.noxcrew.noxesium.sync.menu.NoxesiumSyncSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NoxesiumSettingsScreen.class, remap = false)
public class NoxesiumSettingsScreenMixin {
    @Inject(method = "addToDeveloperTab", at = @At("RETURN"))
    private void addToDeveloperTab(GridLayout.RowHelper rowHelper, CallbackInfo ci) {
        // Try to add the sync settings sub-screen
        var folderSyncSystem = NoxesiumApi.getInstance().getFeatureOrNull(FolderSyncSystem.class);
        if (folderSyncSystem == null) return;
        if (folderSyncSystem.getCurrentSyncedFolders().isEmpty()) return;
        var settingsScreen = (NoxesiumSettingsScreen) (Object) this;
        rowHelper.addChild(Button.builder(Component.translatable("noxesium.options.sync.folders"), (button) -> {
                    Minecraft.getInstance().setScreen(new NoxesiumSyncSettingsScreen(settingsScreen));
                })
                .bounds(0, 0, 150, 20)
                .build());
    }
}
