package com.noxcrew.noxesium.mixin.rules.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.entity.SpatialDebuggingRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Unique
    private DebugRenderer.SimpleDebugRenderer noxesium$spatialDebugRenderer = new SpatialDebuggingRenderer();

    @Inject(method = "render", at = @At("RETURN"))
    public void render(
            PoseStack poseStack,
            Frustum frustum,
            MultiBufferSource.BufferSource bufferSource,
            double cameraX,
            double cameraY,
            double cameraZ,
            CallbackInfo ci) {
        if (!NoxesiumMod.getInstance().getConfig().enableQibSystemDebugging) {
            noxesium$spatialDebugRenderer.render(poseStack, bufferSource, cameraX, cameraY, cameraZ);
        }
    }
}
