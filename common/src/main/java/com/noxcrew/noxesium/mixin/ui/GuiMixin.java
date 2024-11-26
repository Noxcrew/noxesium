package com.noxcrew.noxesium.mixin.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.config.NoxesiumConfig;
import com.noxcrew.noxesium.feature.entity.SpatialInteractionEntityTree;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import com.noxcrew.noxesium.feature.ui.CustomMapUiWidget;
import com.noxcrew.noxesium.feature.ui.layer.LayeredDrawExtension;
import com.noxcrew.noxesium.feature.ui.render.NoxesiumUiRenderState;
import com.noxcrew.noxesium.feature.ui.render.screen.NoxesiumScreenRenderState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.function.Supplier;

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
        return FabricLoader.getInstance().isModLoaded("toggle-sprint-display") ? font.lineHeight + 10 : 5;
    }

    /**
     * Renders the text overlay with Noxesium's debugging various text overlays.
     */
    @Unique
    private void noxesium$renderTextOverlay(GuiGraphics graphics, DeltaTracker deltaTracker) {
        var minecraft = Minecraft.getInstance();
        var font = minecraft.font;
        var lineOffset = font.lineHeight + 5;
        var baseOffset = noxesium$getBaseTextOffset(font);

        // Determine which lines to show
        var text = new ArrayList<Component>();
        if (NoxesiumMod.getInstance().getConfig().showFpsOverlay) {
            text.add(Component.translatable("debug.fps_overlay", minecraft.getFps()));
        }
        if (NoxesiumMod.getInstance().getConfig().showGameTimeOverlay) {
            text.add(Component.translatable("debug.game_time_overlay", String.format("%.5f", RenderSystem.getShaderGameTime()), (int) (RenderSystem.getShaderGameTime() * 24000)));
        }

        // Add qib system debug information
        if (NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging && minecraft.player != null) {
            text.add(Component.literal("§bEntities in model: §7" + SpatialInteractionEntityTree.getModelContents().size()));
            text.add(Component.literal("§bIn water: " + (minecraft.player.isInWaterOrRain() ? "§aYes" : minecraft.player.noxesium$hasTridentCoyoteTime() ? "§eGrace" : "§cNo")));
            text.add(Component.literal("§bQib behavior amount: §7" + ServerRules.QIB_BEHAVIORS.getValue().size()));
        }

        // Show extra text if the experimental patches are on
        if (NoxesiumConfig.experimentalPatchesHotkey != null) {
            text.add(Component.translatable("debug.experimental_patches." + (NoxesiumConfig.experimentalPatchesHotkey ? "on" : "off")));
        }

        // If the experimental patches are on we draw the current UI frame rates and group layouts
        if (NoxesiumMod.getInstance().getConfig().showOptimizationOverlay &&
                !NoxesiumMod.getInstance().getConfig().shouldDisableExperimentalPerformancePatches()) {
            NoxesiumMod.forEachRenderStateHolder((it) -> {
                var stateIn = it.get();
                switch (stateIn) {
                    case NoxesiumUiRenderState state -> {
                        for (var group : state.groups()) {
                            var dynamic = group.dynamic();
                            text.add(Component.literal("§b" + group.layerNames() + (group.dynamic().buffers() > 1 ? " §3(+" + (group.dynamic().buffers() - 1) + ")" : "") + (group.dynamic().isEmpty() ? " §9(empty)" : "") + ": §f" + dynamic.framerate()));
                        }
                    }
                    case NoxesiumScreenRenderState state -> {
                        // Only show this if we are currently running the screen optimizations
                        if (Minecraft.getInstance().screen instanceof MenuAccess<?> ||
                                Minecraft.getInstance().screen instanceof ChatScreen) {
                            var dynamic = state.dynamic();
                            text.add(Component.literal("§eScreen: §f" + dynamic.framerate()));
                        }
                    }
                    case null, default -> {
                    }
                }
            });
        }

        // Draw all the lines in order
        for (int index = 0; index < text.size(); index++) {
            var line = text.get(index);
            var offset = baseOffset + (lineOffset * index);
            graphics.fill(3, offset - 2, 6 + font.width(line), offset + 1 + font.lineHeight, -1873784752);
            graphics.drawString(font, line, 5, offset, 0xE0E0E0, false);
        }
    }

    /**
     * Adds a new rendered element to the UI with the given condition and layer.
     */
    @Unique
    private void noxesium$addRenderLayer(String name, LayeredDraw.Layer layer, Supplier<Boolean> condition) {
        ((LayeredDrawExtension) this.layers).noxesium$addLayer(name, ((guiGraphics, deltaTracker) -> {
            // Check that the main GUI is not hidden
            if (Minecraft.getInstance().options.hideGui) return;

            // Check that the condition is met
            if (condition.get()) {
                layer.render(guiGraphics, deltaTracker);
            }
        }));
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(Minecraft minecraft, CallbackInfo ci) {
        noxesium$addRenderLayer("Noxesium Map UI", new CustomMapUiWidget(), () -> NoxesiumMod.getInstance().getConfig().shouldRenderMapsInUi() &&
                !ServerRules.DISABLE_MAP_UI.getValue());

        noxesium$addRenderLayer("Noxesium Text Overlay", this::noxesium$renderTextOverlay, () -> !this.getDebugOverlay().showDebugScreen() &&
                (NoxesiumMod.getInstance().getConfig().showFpsOverlay ||
                        NoxesiumMod.getInstance().getConfig().showGameTimeOverlay ||
                        NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging ||
                        NoxesiumConfig.experimentalPatchesHotkey != null)
        );
    }
}
