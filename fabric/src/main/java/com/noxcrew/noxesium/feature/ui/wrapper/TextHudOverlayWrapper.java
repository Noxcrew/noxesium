package com.noxcrew.noxesium.feature.ui.wrapper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.config.NoxesiumConfig;
import com.noxcrew.noxesium.feature.entity.SpatialInteractionEntityTree;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import com.noxcrew.noxesium.mixin.component.ext.MinecraftExt;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Adds a custom layer for Noxesium's text HUD overlays.
 */
public class TextHudOverlayWrapper extends ElementWrapper {

    public TextHudOverlayWrapper() {
        // Redraw the HUD every client tick which is plenty frequent yet not every frame.
        registerVariable("client tick", (minecraft, partialTicks) -> ((MinecraftExt) minecraft).getClientTickCount());
    }

    /**
     * Returns the base offset of the text overlay.
     */
    public int getBaseTextOffset(Font font) {
        return FabricLoader.getInstance().isModLoaded("toggle-sprint-display") ? font.lineHeight + 10 : 5;
    }

    @Override
    protected void render(GuiGraphics graphics, Minecraft minecraft, int screenWidth, int screenHeight, Font font, DeltaTracker deltaTracker) {
        var lineOffset = font.lineHeight + 5;
        var baseOffset = getBaseTextOffset(font);

        // Determine which lines to show
        var text = new ArrayList<Component>();
        if (NoxesiumMod.getInstance().getConfig().showFpsOverlay) {
            text.add(Component.translatable("debug.fps_overlay", minecraft.getFps()));

            if (NoxesiumConfig.experimentalPatchesHotkey != null) {
                text.add(Component.translatable("debug.noxesium_overlay." + (NoxesiumConfig.experimentalPatchesHotkey ? "on" : "off")));
            }
        }
        if (NoxesiumMod.getInstance().getConfig().showGameTimeOverlay) {
            text.add(Component.translatable("debug.game_time_overlay", String.format("%.5f", RenderSystem.getShaderGameTime()), (int) (RenderSystem.getShaderGameTime() * 24000)));
        }
        if (NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging && minecraft.player != null) {
            text.add(Component.literal("§bEntities in model: §7" + SpatialInteractionEntityTree.getModelContents().size()));
            text.add(Component.literal("§bIn water: " + (minecraft.player.isInWaterOrRain() ? "§aYes" : "§cNo")));
            text.add(Component.literal("§bQib behavior amount: §7" + ServerRules.QIB_BEHAVIORS.getValue().size()));
        }

        // Draw all the lines in order
        for (int index = 0; index < text.size(); index++) {
            var line = text.get(index);
            var offset = baseOffset + (lineOffset * index);
            graphics.fill(3, offset - 2, 6 + font.width(line), offset + 1 + font.lineHeight, -1873784752);
            graphics.drawString(font, line, 5, offset, 0xE0E0E0, false);
        }
    }
}
