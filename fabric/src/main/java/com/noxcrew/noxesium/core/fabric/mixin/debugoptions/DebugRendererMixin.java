package com.noxcrew.noxesium.core.fabric.mixin.debugoptions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.noxcrew.noxesium.api.client.DebugOption;
import com.noxcrew.noxesium.api.registry.GameComponents;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
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
        if (renderChunkborder) {
            var restrictedOptions =
                    GameComponents.getInstance().noxesium$getComponent(CommonGameComponentTypes.RESTRICT_DEBUG_OPTIONS);
            if (restrictedOptions != null && restrictedOptions.contains(DebugOption.CHUNK_BOUNDARIES.getKeyCode())) {
                renderChunkborder = false;
            }
        }
    }
}
