package com.noxcrew.noxesium.fabric.mixin.debugoptions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.noxcrew.noxesium.api.client.DebugOption;
import com.noxcrew.noxesium.fabric.feature.rule.ServerRules;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Shadow
    private boolean renderChunkborder;

    @Inject(method = "render", at = @At("HEAD"))
    private void restrictChunkBorderRendering(
            PoseStack poseStack,
            Frustum frustum,
            MultiBufferSource.BufferSource bufferSource,
            double x,
            double y,
            double z,
            CallbackInfo ci) {
        if (renderChunkborder
                && ServerRules.RESTRICT_DEBUG_OPTIONS != null
                && ServerRules.RESTRICT_DEBUG_OPTIONS.getValue().contains(DebugOption.CHUNK_BOUNDARIES.getKeyCode())) {
            renderChunkborder = false;
        }
    }
}
