package com.noxcrew.noxesium.mixin.rules.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.noxcrew.noxesium.feature.entity.ExtraEntityData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Guardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Hooks into the guardian rendering code to allow the color and alpha value of them to be changed.
 */
@Mixin(GuardianRenderer.class)
public class GuardianBeamColorRendererMixin {

    /**
     * Swap out the type of rendering for a transparent one so we can properly make it transparent.
     */
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    private static RenderType determineRenderType(ResourceLocation resourceLocation) {
        return RenderType.entityTranslucent(resourceLocation);
    }

    /**
     * Override all vertex drawing so we can modify the color and alpha value.
     */
    @WrapOperation(method = "render(Lnet/minecraft/world/entity/monster/Guardian;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/GuardianRenderer;vertex(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;FFFIIIFF)V"))
    private void overrideColor(
            VertexConsumer vertexConsumer,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            int r,
            int g,
            int b,
            float u,
            float v,
            Operation<Void> original,
            Guardian guardian,
            float f,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light) {

        vertexConsumer.addVertex(pose, x, y, z);

        // Load the color of the beam from the beam color data
        var color = guardian.noxesium$getExtraData(ExtraEntityData.BEAM_COLOR);
        if (color.isPresent()) {
            vertexConsumer.setColor(color.get().getRGB());
        } else {
            vertexConsumer.setColor(r, g, b, 255);
        }

        vertexConsumer.setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
