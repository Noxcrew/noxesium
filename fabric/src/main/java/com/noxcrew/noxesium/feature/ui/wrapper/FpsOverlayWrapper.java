package com.noxcrew.noxesium.feature.ui.wrapper;

import com.noxcrew.noxesium.config.NoxesiumConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Adds a custom layer for Noxesium's fps overlay.
 */
public class FpsOverlayWrapper extends ElementWrapper {

    public FpsOverlayWrapper() {
        registerVariable("fps", (minecraft, partialTicks) -> Minecraft.getInstance().getFps());
    }

    @Override
    protected void render(GuiGraphics graphics, Minecraft minecraft, int screenWidth, int screenHeight, Font font, DeltaTracker deltaTracker) {
        var lineOffset = font.lineHeight + 5;
        var offset = FabricLoader.getInstance().isModLoaded("toggle-sprint-display") ? lineOffset : 0;

        var fps = Component.translatable("debug.fps_overlay", (int) getVariable("fps"));
        graphics.fill(3, 3 + offset, 6 + font.width(fps), 6 + font.lineHeight + offset, -1873784752);
        graphics.drawString(font, fps, 5, 5 + offset, 0xE0E0E0, false);

        if (NoxesiumConfig.experimentalPatchesHotkey == null) return;

        var noxesium = Component.translatable("debug.noxesium_overlay." + (NoxesiumConfig.experimentalPatchesHotkey ? "on" : "off"));
        graphics.fill(3, 3 + offset + lineOffset, 6 + font.width(noxesium), 6 + font.lineHeight + offset + lineOffset, -1873784752);
        graphics.drawString(font, noxesium, 5, 5 + offset + lineOffset, 0xE0E0E0, false);
    }
}
