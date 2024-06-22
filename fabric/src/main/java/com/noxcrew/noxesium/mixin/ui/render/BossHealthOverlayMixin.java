package com.noxcrew.noxesium.mixin.ui.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.feature.ui.cache.ElementManager;
import com.noxcrew.noxesium.feature.ui.cache.BossBarWrapper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {

    @WrapOperation(method = "render", at = @At("HEAD"))
    public void render(GuiGraphics graphics, Operation<Void> original) {
        ElementManager.getInstance(BossBarWrapper.class).wrapOperation(graphics, original);
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void refreshBossBarOnUpdate(CallbackInfo ci) {
        ElementManager.getInstance(BossBarWrapper.class).requestRedraw();
    }

    @Inject(method = "reset", at = @At("TAIL"))
    private void refreshBossBarOnReset(CallbackInfo ci) {
        ElementManager.getInstance(BossBarWrapper.class).requestRedraw();
    }
}
