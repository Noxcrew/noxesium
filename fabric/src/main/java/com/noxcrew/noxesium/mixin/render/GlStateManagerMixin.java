package com.noxcrew.noxesium.mixin.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.noxcrew.noxesium.feature.render.cache.ElementCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Overrides the blending state to enforce blending to be on for some part of the code.
 */
@Mixin(value = GlStateManager.class, remap = false)
public class GlStateManagerMixin {

    @Inject(method = "_enableBlend", at = @At("HEAD"), cancellable = true)
    private static void _enableBlend(CallbackInfo ci) {
        if (ElementCache.allowBlendChanges) return;
        ci.cancel();
    }

    @Inject(method = "_disableBlend", at = @At("HEAD"), cancellable = true)
    private static void _disableBlend(CallbackInfo ci) {
        if (ElementCache.allowBlendChanges) return;
        ci.cancel();
    }

    @Inject(method = "_blendFunc", at = @At("HEAD"), cancellable = true)
    private static void _blendFunc(int i, int j, CallbackInfo ci) {
        if (ElementCache.allowBlendChanges) return;
        ci.cancel();
    }

    @Inject(method = "_blendFuncSeparate", at = @At("HEAD"), cancellable = true)
    private static void _blendFuncSeparate(int i, int j, int k, int l, CallbackInfo ci) {
        if (ElementCache.allowBlendChanges) return;
        ci.cancel();
    }

    @Inject(method = "_drawElements", at = @At("HEAD"))
    private static void _drawElements(int i, int j, int k, long l, CallbackInfo ci) {
        if (!ElementCache.hasDrawnSomething) {
            ElementCache.hasDrawnSomething = true;
        }
    }
}
