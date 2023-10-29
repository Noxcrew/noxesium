package com.noxcrew.noxesium.mixin.render;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.render.cache.actionbar.ActionBarCache;
import com.noxcrew.noxesium.feature.render.cache.bossbar.BossBarCache;
import com.noxcrew.noxesium.feature.render.cache.chat.ChatCache;
import com.noxcrew.noxesium.feature.render.cache.scoreboard.ScoreboardCache;
import com.noxcrew.noxesium.feature.render.cache.tablist.TabListCache;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    private int screenHeight;

    @Shadow
    private int screenWidth;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Nullable
    private Component overlayMessageString;

    @Inject(method = "renderSavingIndicator", at = @At("RETURN"))
    private void injected(GuiGraphics guiGraphics, CallbackInfo ci) {
        var gui = minecraft.gui;
        var font = minecraft.font;

        // Don't render when the debug screen is shown as it would overlap
        if (gui.getDebugOverlay().showDebugScreen() || !NoxesiumMod.fpsOverlay) return;

        var text = Component.translatable("debug.fps_overlay", Minecraft.getInstance().getFps());
        var offset = FabricLoader.getInstance().isModLoaded("toggle-sprint-display") ? font.lineHeight + 5 : 0;
        guiGraphics.fill(3, 3 + offset, 6 + font.width(text), 6 + font.lineHeight + offset, -1873784752);
        guiGraphics.drawString(font, text, 5, 5 + offset, 0xE0E0E0, false);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/scores/Objective;)V"))
    private void injected(Gui instance, GuiGraphics guiGraphics, Objective objective) {
        if (NoxesiumMod.disablePatches) {
            instance.displayScoreboardSidebar(guiGraphics, objective);
        } else {
            ScoreboardCache.getInstance().renderDirect(guiGraphics, ScoreboardCache.getInstance().getCache(), screenWidth, screenHeight, minecraft);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;render(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void injected(BossHealthOverlay instance, GuiGraphics guiGraphics) {
        if (NoxesiumMod.disablePatches) {
            instance.render(guiGraphics);
        } else {
            BossBarCache.getInstance().renderDirect(guiGraphics, BossBarCache.getInstance().getCache(), screenWidth, screenHeight, minecraft);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;render(Lnet/minecraft/client/gui/GuiGraphics;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V"))
    private void injected(PlayerTabOverlay instance, GuiGraphics guiGraphics, int width, Scoreboard scoreboard, Objective objective) {
        if (NoxesiumMod.disablePatches) {
            instance.render(guiGraphics, width, scoreboard, objective);
        } else {
            TabListCache.getInstance().renderDirect(guiGraphics, TabListCache.getInstance().getCache(), width, screenHeight, minecraft);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lnet/minecraft/client/gui/GuiGraphics;III)V"))
    private void injected(ChatComponent instance, GuiGraphics guiGraphics, int ticks, int mouseX, int mouseY) {
        if (NoxesiumMod.disablePatches) {
            instance.render(guiGraphics, ticks, mouseX, mouseY);
        } else {
            ChatCache.lastTick = ticks;
            ChatCache.mouseX = mouseX;
            ChatCache.mouseY = mouseY;
            ChatCache.getInstance().renderDirect(guiGraphics, ChatCache.getInstance().getCache(), screenWidth, screenHeight, minecraft);
        }
    }

    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Gui;overlayMessageString:Lnet/minecraft/network/chat/Component;"))
    private Component injected(Gui instance) {
        // Prevent the normal action bar from rendering!
        if (NoxesiumMod.disablePatches) {
            return overlayMessageString;
        } else {
            ActionBarCache.getInstance().renderDirect(ActionBarCache.graphics, ActionBarCache.getInstance().getCache(), screenWidth, screenHeight, minecraft);
            return null;
        }
    }

    @Inject(method = "render", at = @At(value = "HEAD"))
    private void render(GuiGraphics guiGraphics, float partialTicks, CallbackInfo ci) {
        // Store the current partial ticks and graphics at the start of the method as we need it later in the redirect
        ActionBarCache.lastPartialTicks = partialTicks;
        ActionBarCache.graphics = guiGraphics;
    }

    @Inject(method = "setOverlayMessage", at = @At(value = "TAIL"))
    private void setOverlayMessage(Component component, boolean bl, CallbackInfo ci) {
        ActionBarCache.getInstance().clearCache();
    }
}
