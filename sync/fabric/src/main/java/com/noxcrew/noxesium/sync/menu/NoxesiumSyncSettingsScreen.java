package com.noxcrew.noxesium.sync.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

/**
 * A screen for configuring which folders should be used for synchronization.
 */
public class NoxesiumSyncSettingsScreen extends OptionsSubScreen {

    public NoxesiumSyncSettingsScreen(Screen screen) {
        super(screen, Minecraft.getInstance().options, Component.translatable("noxesium.options.screen.sync"));
    }

    @Override
    protected void addOptions() {

    }
}
