package com.noxcrew.noxesium.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

/**
 * The custom settings screen used by Noxesium which opens when clicking on Noxesium in Mod Menu or by pressing F3+Y.
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
                NoxesiumOptions.dumpIncomingPackets(),
                NoxesiumOptions.dumpOutgoingPackets(),
                NoxesiumOptions.extendedPacketLogging(),
                NoxesiumOptions.playerGlowingKeybinds(),
                NoxesiumOptions.qibSystemDebugVisuals()
        );
        this.list.addBig(NoxesiumOptions.experimentalPatches());
        this.list.addSmall(
                NoxesiumOptions.minUiFramerate(),
                NoxesiumOptions.maxUiFramerate(),
                NoxesiumOptions.optimizationOverlay()
        );
    }
}
