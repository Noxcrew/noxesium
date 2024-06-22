package com.noxcrew.noxesium.mixin.ui.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.ui.cache.ElementManager;
import com.noxcrew.noxesium.feature.ui.cache.ActionBarWrapper;
import com.noxcrew.noxesium.feature.ui.cache.ChatWrapper;
import com.noxcrew.noxesium.feature.ui.cache.FpsOverlayWrapper;
import com.noxcrew.noxesium.feature.ui.cache.GameTimeOverlayWrapper;
import com.noxcrew.noxesium.feature.ui.cache.ScoreboardWrapper;
import com.noxcrew.noxesium.feature.ui.cache.TitleWrapper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

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
    public abstract DebugScreenOverlay getDebugOverlay();

    @Shadow
    public abstract ChatComponent getChat();

    /**
     * Adds a new rendered element to the UI with the given condition and layer.
     */
    @Unique
    private void noxesium$addRenderLayer(LayeredDraw.Layer layer, Supplier<Boolean> condition) {
        var overlay = new LayeredDraw();
        overlay.add(layer);
        this.layers.add(overlay, () ->
                // Check that the main GUI is not hidden
                !Minecraft.getInstance().options.hideGui &&
                        // Check that the debug screen is not up
                        !this.getDebugOverlay().showDebugScreen() &&
                        // Check that the condition is met
                        condition.get()
        );
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(Minecraft minecraft, CallbackInfo ci) {
        noxesium$addRenderLayer(ElementManager.getInstance(FpsOverlayWrapper.class)::render, () -> NoxesiumMod.getInstance().getConfig().showFpsOverlay);
        noxesium$addRenderLayer(ElementManager.getInstance(GameTimeOverlayWrapper.class)::render, () -> NoxesiumMod.getInstance().getConfig().showGameTimeOverlay);
    }

    @WrapOperation(method = "renderScoreboardSidebar", at = @At("HEAD"))
    public void renderScoreboardSidebar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        ElementManager.getInstance(ScoreboardWrapper.class).wrapOperation(guiGraphics, deltaTracker, original);
    }

    @WrapOperation(method = "renderOverlayMessage", at = @At("HEAD"))
    public void renderOverlayMessage(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        ElementManager.getInstance(ActionBarWrapper.class).wrapOperation(guiGraphics, deltaTracker, original);
    }

    @WrapOperation(method = "renderTitle", at = @At("HEAD"))
    public void renderTitle(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        ElementManager.getInstance(TitleWrapper.class).wrapOperation(guiGraphics, deltaTracker, original);
    }

    @WrapOperation(method = "renderChat", at = @At("HEAD"))
    public void renderChat(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        ElementManager.getInstance(ChatWrapper.class).wrapOperation(guiGraphics, deltaTracker, original);
    }
}
