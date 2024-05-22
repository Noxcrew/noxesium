package com.noxcrew.noxesium.mixin.performance.render;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.render.cache.actionbar.ActionBarCache;
import com.noxcrew.noxesium.feature.render.cache.chat.ChatCache;
import com.noxcrew.noxesium.feature.render.cache.fps.FpsOverlayCache;
import com.noxcrew.noxesium.feature.render.cache.scoreboard.ScoreboardCache;
import com.noxcrew.noxesium.feature.render.cache.title.TitleCache;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into Gui and replaces them one-by-one with Noxesiums features. Since 1.20.5 Mojang draws
 * all elements in their own methods, so we can much more easily hook into specific ones and
 * change them around without having to deal with the rest of the methods.
 * <p>
 * Boss bar overlay, debug overlay & (audio) sub-title overlay are handled in separate mixins.
 * The sleep overlay is not changed.
 */
@Mixin(Gui.class)
public abstract class GuiHookMixin {

    @Shadow
    @Final
    private LayeredDraw layers;

    @Shadow
    private int tickCount;

    @Shadow
    public abstract DebugScreenOverlay getDebugOverlay();

    @Shadow
    public abstract ChatComponent getChat();

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(Minecraft minecraft, CallbackInfo ci) {
        var overlay = new LayeredDraw();
        overlay.add(FpsOverlayCache::renderFpsOverlay);
        this.layers.add(overlay, () ->
                // Check that the main GUI is not hidden
                !minecraft.options.hideGui &&
                        // Check that the debug screen is not up
                        !this.getDebugOverlay().showDebugScreen() &&
                        // Check that the setting is enabled
                        NoxesiumMod.getInstance().getConfig().showFpsOverlay
        );
    }

    @Inject(method = "renderCameraOverlays", at = @At("HEAD"), cancellable = true)
    public void renderCameraOverlays(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        // TODO implement optimizations
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    public void renderCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        // TODO implement optimizations
    }

    @Inject(method = "renderHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    public void renderHotbarAndDecorations(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        // TODO implement optimizations
    }

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    public void renderEffects(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        // TODO implement optimizations
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    public void renderScoreboardSidebar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (NoxesiumMod.getInstance().getConfig().shouldDisableExperimentalPerformancePatches()) return;
        ci.cancel();
        ScoreboardCache.getInstance().render(guiGraphics, deltaTracker);
    }

    @Inject(method = "renderOverlayMessage", at = @At("HEAD"), cancellable = true)
    public void renderOverlayMessage(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (NoxesiumMod.getInstance().getConfig().shouldDisableExperimentalPerformancePatches()) return;
        ci.cancel();
        ActionBarCache.getInstance().render(guiGraphics, deltaTracker);
    }

    @Inject(method = "renderTitle", at = @At("HEAD"), cancellable = true)
    public void renderTitle(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (NoxesiumMod.getInstance().getConfig().shouldDisableExperimentalPerformancePatches()) return;
        ci.cancel();
        TitleCache.getInstance().render(guiGraphics, deltaTracker);
    }

    @Inject(method = "renderChat", at = @At("HEAD"), cancellable = true)
    public void renderChat(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (NoxesiumMod.getInstance().getConfig().shouldDisableExperimentalPerformancePatches()) return;
        ci.cancel();

        // Only render when not focussed!
        if (this.getChat().isChatFocused()) return;

        // Store some variables on the cache before rendering
        var minecraft = Minecraft.getInstance();
        var window = minecraft.getWindow();
        ChatCache.mouseX = Mth.floor(minecraft.mouseHandler.xpos() * (double) window.getGuiScaledWidth() / (double) window.getScreenWidth());
        ChatCache.mouseY = Mth.floor(minecraft.mouseHandler.ypos() * (double) window.getGuiScaledHeight() / (double) window.getScreenHeight());
        ChatCache.lastTick = this.tickCount;
        ChatCache.getInstance().render(guiGraphics, deltaTracker);
    }

    @Inject(method = "renderSavingIndicator", at = @At("HEAD"), cancellable = true)
    public void renderSavingIndicator(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        // TODO implement optimizations
    }
}
