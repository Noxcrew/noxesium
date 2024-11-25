package com.noxcrew.noxesium.mixin.ui;

import com.noxcrew.noxesium.feature.ui.render.SharedVertexBuffer;
import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into clearing of the blend render state and ignores blending changes.
 */
@Mixin(RenderStateShard.class)
public abstract class RenderStateShardMixin {

    @Inject(method = "clearRenderState", at = @At("HEAD"))
    private void onStartClearingRenderState(CallbackInfo ci) {
        SharedVertexBuffer.ignoreBlendStateHook = true;
    }

    @Inject(method = "clearRenderState", at = @At("TAIL"))
    private void onDoneClearingRenderState(CallbackInfo ci) {
        SharedVertexBuffer.ignoreBlendStateHook = false;
    }
}
