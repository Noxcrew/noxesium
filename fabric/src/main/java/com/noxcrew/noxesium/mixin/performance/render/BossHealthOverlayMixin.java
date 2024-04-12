package com.noxcrew.noxesium.mixin.performance.render;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.render.cache.bossbar.BossBarCache;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(GuiGraphics graphics, CallbackInfo ci) {
        if (NoxesiumMod.getInstance().getConfig().shouldDisableExperimentalPerformancePatches()) return;
        ci.cancel();
        BossBarCache.getInstance().render(graphics, 0f);
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void refreshBossBarOnUpdate(CallbackInfo ci) {
        BossBarCache.getInstance().clearCache();
    }

    @Inject(method = "reset", at = @At("TAIL"))
    private void refreshBossBarOnReset(CallbackInfo ci) {
        BossBarCache.getInstance().clearCache();
    }
}
