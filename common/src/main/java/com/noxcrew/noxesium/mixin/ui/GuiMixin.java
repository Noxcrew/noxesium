package com.noxcrew.noxesium.mixin.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.CustomMapUiWidget;
import com.noxcrew.noxesium.feature.entity.SpatialInteractionEntityTree;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import java.util.ArrayList;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
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
    @Final
    private LayeredDraw layers;

    @Shadow
    public abstract DebugScreenOverlay getDebugOverlay();

    /**
     * Returns the base offset of the text overlay.
     */
    @Unique
    private int noxesium$getBaseTextOffset(Font font) {
        // TODO Hardcoded checks for other mods are still rubbish!
        return NoxesiumMod.getPlatform().isModLoaded("toggle-sprint-display") ? font.lineHeight + 10 : 5;
    }

    /**
     * Renders the text overlay with Noxesium's debugging various text overlays.
     */
    @Unique
    private void noxesium$renderTextOverlay(GuiGraphics graphics, DeltaTracker deltaTracker) {
        var minecraft = Minecraft.getInstance();

        // Check that the main GUI is not hidden
        if (minecraft.options.hideGui) return;

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
            text.add(Component.translatable(
                    "debug.game_time_overlay", String.format("%.5f", RenderSystem.getShaderGameTime()), (int)
                            (RenderSystem.getShaderGameTime() * 24000)));
        }

        // Add debug overlays if enabled, these are not using translations as they are purely for debugging purposes!
        // Start with qib system debug information
        if (NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging && minecraft.player != null) {
            text.add(Component.literal("§bEntities in model: §7"
                    + SpatialInteractionEntityTree.getModelContents().size()));
            text.add(Component.literal("§bIn water: "
                    + (minecraft.player.isInWaterOrRain()
                            ? "§aYes"
                            : minecraft.player.noxesium$hasTridentCoyoteTime() ? "§eGrace" : "§cNo")));
            text.add(Component.literal("§bQib behavior amount: §7"
                    + ServerRules.QIB_BEHAVIORS.getValue().size()));
        }

        // Draw all the lines in order
        for (int index = 0; index < text.size(); index++) {
            var line = text.get(index);
            var offset = baseOffset + (lineOffset * index);
            graphics.fill(3, offset - 2, 6 + font.width(line), offset + 1 + font.lineHeight, -1873784752);
            graphics.drawString(font, line, 5, offset, 0xE0E0E0, false);
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(Minecraft minecraft, CallbackInfo ci) {
        this.layers.add(new CustomMapUiWidget());
        this.layers.add(this::noxesium$renderTextOverlay);
    }
}
