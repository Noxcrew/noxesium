package com.noxcrew.noxesium.core.fabric.config;

import net.minecraft.client.Minecraft;
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
    }
}
