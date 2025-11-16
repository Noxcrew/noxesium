package com.noxcrew.noxesium.config;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
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
        var options = new ArrayList<OptionInstance<?>>();
        options.add(NoxesiumOptions.gameTimeOverlay());
        options.add(NoxesiumOptions.playerGlowingKeybinds());
        options.add(NoxesiumOptions.dumpIncomingPackets());
        options.add(NoxesiumOptions.dumpOutgoingPackets());
        options.add(NoxesiumOptions.qibSystemDebugVisuals());
        options.add(NoxesiumOptions.debugScoreboardTeams());
        options.add(NoxesiumOptions.extendedPacketLogging());
        if (Minecraft.getInstance().player == null
                || Minecraft.getInstance().player.getPermissionLevel() >= 2) {
            options.add(NoxesiumOptions.showCullingBoxes());
        }
        this.list.addSmall(options.toArray(new OptionInstance[0]));
    }
}
