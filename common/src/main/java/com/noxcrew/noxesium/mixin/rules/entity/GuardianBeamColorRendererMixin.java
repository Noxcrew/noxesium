package com.noxcrew.noxesium.mixin.rules.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.noxcrew.noxesium.feature.entity.ExtraEntityData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.client.renderer.entity.state.GuardianRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Guardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

/**
 * Hooks into the guardian rendering code to allow the color and alpha value of them to be changed.
 */
@Mixin(GuardianRenderer.class)
public class GuardianBeamColorRendererMixin {

    @Unique
    private static Integer noxesium$beamColor = null;

    @Unique
    private static Integer noxesium$beamColorFade = null;

    @Unique
    private static int noxesium$index = 0;

    /**
     * Sets the current beam color on the render state.
     */
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/Guardian;Lnet/minecraft/client/renderer/entity/state/GuardianRenderState;F)V", at = @At("RETURN"))
    public void includeBeamInformation(Guardian guardian, GuardianRenderState guardianRenderState, float f, CallbackInfo ci) {
        var color = guardian.noxesium$getExtraData(ExtraEntityData.BEAM_COLOR).map(Color::getRGB).orElse(null);
        var fade = guardian.noxesium$getExtraData(ExtraEntityData.BEAM_COLOR_FADE).map(Color::getRGB).orElse(null);
        guardianRenderState.noxesium$setBeamColor(color, fade);
    }

    /**
     * Swap out the type of rendering for a transparent one so we can properly make it transparent.
     */
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    private static RenderType determineRenderType(ResourceLocation resourceLocation) {
        return RenderType.entityTranslucent(resourceLocation);
    }

    /**
     * Prepare the color when we begin rendering.
     */
    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/GuardianRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void prepareColor(GuardianRenderState guardianRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        noxesium$beamColor = guardianRenderState.noxesium$getBeamColor();
        noxesium$beamColorFade = guardianRenderState.noxesium$getBeamColorFade();
        noxesium$index = 0;
    }

    /**
     * Override the vertex color used when drawing.
     */
    @Redirect(method = "vertex", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(IIII)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private static VertexConsumer overrideColor(
            VertexConsumer vertexConsumer, int r, int g, int b, int a
    ) {
        if (noxesium$beamColor != null) {
            if (noxesium$beamColorFade != null) {
                var ind = noxesium$index;
                noxesium$index++;
                if (noxesium$index >= 12) {
                    noxesium$index = 0;
                }
                if (ind == 0 || ind == 3 || ind == 4 || ind >= 7) {
                    vertexConsumer.setColor(noxesium$beamColorFade);
                } else {
                    vertexConsumer.setColor(noxesium$beamColor);
                }
            } else {
                vertexConsumer.setColor(noxesium$beamColor);
            }
        } else {
            vertexConsumer.setColor(r, g, b, 255);
        }
        return vertexConsumer;
    }
}
