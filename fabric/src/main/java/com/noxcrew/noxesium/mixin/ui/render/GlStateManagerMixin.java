package com.noxcrew.noxesium.mixin.ui.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.noxcrew.noxesium.feature.ui.cache.ElementWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Overrides the blending state to enforce blending to be on for some part of the code.
 */
@Mixin(value = GlStateManager.class, remap = false)
public abstract class GlStateManagerMixin {

    @Inject(method = "_enableBlend", at = @At("HEAD"), cancellable = true)
    private static void checkElementCacheForEnableBlend(CallbackInfo ci) {
        if (ElementWrapper.allowBlendChanges) return;
        ci.cancel();
    }

    @Inject(method = "_disableBlend", at = @At("HEAD"), cancellable = true)
    private static void checkElementCacheForDisableBlend(CallbackInfo ci) {
        if (ElementWrapper.allowBlendChanges) return;
        ci.cancel();
    }

    @Inject(method = "_blendFunc", at = @At("HEAD"), cancellable = true)
    private static void checkElementCacheForBlendFunc(CallbackInfo ci) {
        if (ElementWrapper.allowBlendChanges) return;
        ci.cancel();
    }

    @Inject(method = "_blendFuncSeparate", at = @At("HEAD"), cancellable = true)
    private static void checkElementCacheForBlendFuncSeparate(CallbackInfo ci) {
        if (ElementWrapper.allowBlendChanges) return;
        ci.cancel();
    }
}
