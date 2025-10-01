package com.noxcrew.noxesium.core.fabric.mixin.ui;

import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.fabric.feature.entity.QibBehaviorModule;
import com.noxcrew.noxesium.core.fabric.feature.render.CustomMapUiWidget;
import java.util.ArrayList;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds additional render layers for Noxesium.
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    public abstract DebugScreenOverlay getDebugOverlay();

    /**
     * Returns the base offset of the text overlay.
     */
    @Unique
    private int noxesium$getBaseTextOffset(Font font) {
        // TODO Hardcoded checks for other mods are still rubbish!
        return FabricLoader.getInstance().isModLoaded("toggle-sprint-display") ? font.lineHeight + 10 : 5;
    }

    /**
     * Renders the text overlay with Noxesium's debugging various text overlays.
     */
    @Unique
    private void noxesium$renderTextOverlay(GuiGraphics graphics, DeltaTracker deltaTracker) {
        var minecraft = Minecraft.getInstance();

        // Check that we have something to show
        if (this.getDebugOverlay().showDebugScreen()
                || (!NoxesiumMod.getInstance().getConfig().showFpsOverlay
                        && !NoxesiumMod.getInstance().getConfig().showGameTimeOverlay)) return;

        var font = minecraft.font;
        var lineOffset = font.lineHeight + 5;
        var baseOffset = noxesium$getBaseTextOffset(font);

        // Determine which lines to show
        var text = new ArrayList<Component>();
        if (NoxesiumMod.getInstance().getConfig().showFpsOverlay) {
            text.add(Component.translatable("debug.fps_overlay", minecraft.getFps()));
        }
        if (NoxesiumMod.getInstance().getConfig().showGameTimeOverlay) {
            var gameTimeInt = minecraft.level == null ? 0L : minecraft.level.getGameTime();
            var gameTimeShader = ((float) (gameTimeInt % 24000L)
                            + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false))
                    / 24000.0;
            text.add(Component.translatable(
                    "debug.game_time_overlay", String.format("%.5f", gameTimeShader), (int) (gameTimeShader * 24000)));
        }

        // Add debug overlays if enabled, these are not using translations as they are purely for debugging purposes!
        // Start with qib system debug information
        if (NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging && minecraft.player != null) {
            var models = QibBehaviorModule.SPATIAL_TREE.getModelContents().size();
            text.add(Component.literal("§bEntities in model: §7" + models));
            text.add(Component.literal("§bIn water: "
                    + (minecraft.player.isInWaterOrRain()
                            ? "§aYes"
                            : minecraft.player.noxesium$hasTridentCoyoteTime() ? "§eGrace" : "§cNo")));
            text.add(Component.literal("§bQib behavior amount: §7"
                    + NoxesiumRegistries.QIB_EFFECTS.getContents().size()));
        }

        // Draw all the lines in order
        if (text.isEmpty()) return;
        graphics.nextStratum();
        for (int index = 0; index < text.size(); index++) {
            var line = text.get(index);
            var offset = baseOffset + (lineOffset * index);
            graphics.fill(3, offset - 2, 6 + font.width(line), offset + 1 + font.lineHeight, -1873784752);
            graphics.drawString(font, line, 5, offset, ARGB.color(255, 0xE0E0E0), false);
        }
    }

    @Inject(method = "renderDemoOverlay", at = @At("HEAD"))
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        // Render extra overlays around the demo overlay
        CustomMapUiWidget.render(graphics, deltaTracker);
        noxesium$renderTextOverlay(graphics, deltaTracker);
    }
}
