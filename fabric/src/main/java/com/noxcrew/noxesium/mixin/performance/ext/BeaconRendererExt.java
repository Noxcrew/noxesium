package com.noxcrew.noxesium.mixin.performance.ext;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BeaconRenderer.class)
public interface BeaconRendererExt {

    @Invoker("renderPart")
    static void invokeRenderPart(PoseStack poseStack, VertexConsumer vertexConsumer, float f, float g, float h, float i, int j, int k, float l, float m, float n, float o, float p, float q, float r, float s, float t, float u, float v, float w) {
        throw new AssertionError("Unimplemented");
    }
}
