package com.noxcrew.noxesium.feature.ui.cache;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Adds a custom layer for Noxesium's game time overlay.
 */
public class GameTimeOverlayWrapper extends ElementWrapper {

    public GameTimeOverlayWrapper() {
        registerVariable("game time", (minecraft, partialTicks) -> RenderSystem.getShaderGameTime());
    }

    @Override
    protected void render(GuiGraphics graphics, Minecraft minecraft, int screenWidth, int screenHeight, Font font, DeltaTracker deltaTracker) {
        var lineOffset = font.lineHeight + 5;
        var offset = FabricLoader.getInstance().isModLoaded("toggle-sprint-display") ? lineOffset * 2 : lineOffset;

        var fps = Component.translatable("debug.game_time_overlay", String.format("%.5f", (float) getVariable("game time")), (int) ((float) getVariable("game time") * 24000));
        graphics.fill(3, 3 + offset, 6 + font.width(fps), 6 + font.lineHeight + offset, -1873784752);
        graphics.drawString(font, fps, 5, 5 + offset, 0xE0E0E0, false);
    }
}
