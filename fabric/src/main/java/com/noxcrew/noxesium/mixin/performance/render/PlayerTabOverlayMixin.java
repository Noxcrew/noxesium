package com.noxcrew.noxesium.mixin.performance.render;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.render.cache.tablist.TabListCache;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void renderTabList(GuiGraphics graphics, int partialTicks, Scoreboard scoreboard, Objective objective, CallbackInfo ci) {
        if (NoxesiumMod.getInstance().getConfig().shouldDisableExperimentalPerformancePatches()) return;
        ci.cancel();
        TabListCache.getInstance().render(graphics, partialTicks);
    }

    @Inject(method = "setHeader", at = @At("TAIL"))
    private void refreshTabListOnSetHeader(CallbackInfo ci) {
        TabListCache.getInstance().clearCache();
    }

    @Inject(method = "setFooter", at = @At("TAIL"))
    private void refreshTabListOnSetFooter(CallbackInfo ci) {
        TabListCache.getInstance().clearCache();
    }

    @Inject(method = "reset", at = @At("TAIL"))
    private void refreshTabListOnReset(CallbackInfo ci) {
        TabListCache.getInstance().clearCache();
    }

    @Inject(method = "setVisible", at = @At(value = "INVOKE", target = "Ljava/util/Map;clear()V"))
    private void refreshTabListOnSetVisible(CallbackInfo ci) {
        TabListCache.getInstance().resetHearts();
    }
}
