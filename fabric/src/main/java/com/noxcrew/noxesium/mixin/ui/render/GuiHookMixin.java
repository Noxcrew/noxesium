package com.noxcrew.noxesium.mixin.ui.render;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.ui.wrapper.ActionBarWrapper;
import com.noxcrew.noxesium.feature.ui.wrapper.BossBarWrapper;
import com.noxcrew.noxesium.feature.ui.wrapper.ChatWrapper;
import com.noxcrew.noxesium.feature.ui.wrapper.DebugWrapper;
import com.noxcrew.noxesium.feature.ui.wrapper.ElementManager;
import com.noxcrew.noxesium.feature.ui.wrapper.FpsOverlayWrapper;
import com.noxcrew.noxesium.feature.ui.wrapper.GameTimeOverlayWrapper;
import com.noxcrew.noxesium.feature.ui.wrapper.MapUiWrapper;
import com.noxcrew.noxesium.feature.ui.wrapper.ScoreboardWrapper;
import com.noxcrew.noxesium.feature.ui.wrapper.TabListWrapper;
import com.noxcrew.noxesium.feature.ui.wrapper.TitleWrapper;
import com.noxcrew.noxesium.mixin.ui.render.ext.ChatComponentExt;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
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

    @Unique
    private int noxesium$lastChatHover;

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
        // Render map at the bottom so the rest goes on top, this goes after vanilla so we render on top of e.g. chat
        noxesium$addRenderLayer(ElementManager.getInstance(MapUiWrapper.class)::render, () -> NoxesiumMod.getInstance().getConfig().shouldRenderMapsInUi());
        noxesium$addRenderLayer(ElementManager.getInstance(FpsOverlayWrapper.class)::render, () -> NoxesiumMod.getInstance().getConfig().showFpsOverlay);
        noxesium$addRenderLayer(ElementManager.getInstance(GameTimeOverlayWrapper.class)::render, () -> NoxesiumMod.getInstance().getConfig().showGameTimeOverlay);
    }

    @WrapMethod(method = "renderScoreboardSidebar")
    public void renderScoreboardSidebar(GuiGraphics graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        ElementManager.getInstance(ScoreboardWrapper.class).wrapOperation(graphics, deltaTracker, () -> original.call(graphics, deltaTracker));
    }

    @WrapMethod(method = "renderOverlayMessage")
    public void renderOverlayMessage(GuiGraphics graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        ElementManager.getInstance(ActionBarWrapper.class).wrapOperation(graphics, deltaTracker, () -> original.call(graphics, deltaTracker));
    }

    @WrapMethod(method = "renderTitle")
    public void renderTitle(GuiGraphics graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        ElementManager.getInstance(TitleWrapper.class).wrapOperation(graphics, deltaTracker, () -> original.call(graphics, deltaTracker));
    }

    @WrapOperation(method = "renderChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V"))
    public void renderChat(ChatComponent instance, GuiGraphics graphics, int tickCount, int x, int y, boolean focussed, Operation<Void> original) {
        var chatWrapper = ElementManager.getInstance(ChatWrapper.class);
        var chatComponentExt = (ChatComponentExt) instance;

        // Update the rendered chat whenever you change which message you hover over (so hover components work properly!)
        var hover = chatComponentExt.invokeGetMessageEndIndexAt(chatComponentExt.invokeScreenToChatX(x), chatComponentExt.invokeScreenToChatY(y));
        if (hover != noxesium$lastChatHover) {
            noxesium$lastChatHover = hover;
            chatWrapper.requestRedraw();
        }
        chatWrapper.wrapOperation(graphics, DeltaTracker.ZERO, () -> original.call(instance, graphics, tickCount, x, y, focussed));
    }

    @WrapOperation(method = "renderTabList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;render(Lnet/minecraft/client/gui/GuiGraphics;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V"))
    public void renderTabList(PlayerTabOverlay instance, GuiGraphics graphics, int partialTicks, Scoreboard scoreboard, Objective objective, Operation<Void> original) {
        ElementManager.getInstance(TabListWrapper.class).wrapOperation(graphics, DeltaTracker.ZERO, () -> original.call(instance, graphics, partialTicks, scoreboard, objective));
    }

    @WrapOperation(method = "method_55807", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;render(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    public void render(DebugScreenOverlay instance, GuiGraphics graphics, Operation<Void> original) {
        ElementManager.getInstance(DebugWrapper.class).wrapOperation(graphics, DeltaTracker.ZERO, () -> original.call(instance, graphics));
    }

    @WrapOperation(method = "method_55808", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;render(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    public void render(BossHealthOverlay instance, GuiGraphics graphics, Operation<Void> original) {
        ElementManager.getInstance(BossBarWrapper.class).wrapOperation(graphics, DeltaTracker.ZERO, () -> original.call(instance, graphics));
    }
}
