package com.noxcrew.noxesium.core.fabric.mixin.rules.entity;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes;
import java.awt.Color;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into the end crystal rendering.
 */
@Mixin(EndCrystalRenderer.class)
public class EndCrystalRendererMixin {

    /**
     * Override the custom geometry code to recolor the crystal beam.
     */
    @Redirect(
            method =
                    "submit(Lnet/minecraft/client/renderer/entity/state/EndCrystalRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/entity/EnderDragonRenderer;submitCrystalBeams(FFFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"))
    private void submit(
            float deltaX,
            float deltaY,
            float deltaZ,
            float timeInTicks,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            @Local(argsOnly = true) EndCrystalRenderState endCrystalRenderState) {
        /*
         * Unfortunately because this uses a custom geometry we have to override the entire
         * method and redo all of this logic to change the colors. :/
         */
        float horizontalLength = Mth.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float length = Mth.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        poseStack.pushPose();
        poseStack.translate(0.0F, 2.0F, 0.0F);
        poseStack.mulPose(Axis.YP.rotation((float) -Math.atan2(deltaZ, deltaX) - 1.5707964F));
        poseStack.mulPose(Axis.XP.rotation((float) -Math.atan2(horizontalLength, deltaY) - 1.5707964F));
        float v0 = 0.0F - timeInTicks * 0.01F;
        float v1 = length / 32.0F - timeInTicks * 0.01F;
        var beamColor = endCrystalRenderState.noxesium$getBeamColor() == null
                ? -1
                : endCrystalRenderState.noxesium$getBeamColor();
        var beamColorFade = endCrystalRenderState.noxesium$getBeamColorFade() == null
                ? -16777216
                : endCrystalRenderState.noxesium$getBeamColorFade();
        submitNodeCollector.submitCustomGeometry(poseStack, EnderDragonRenderer.BEAM, (pose, buffer) -> {
            int steps = 8;
            float lastSin = 0.0F;
            float lastCos = 0.75F;
            float lastU = 0.0F;
            for (int i = 1; i <= steps; i++) {
                float sin = Mth.sin((i * 6.2831855F / (float) steps)) * 0.75F;
                float cos = Mth.cos((i * 6.2831855F / (float) steps)) * 0.75F;
                float u = i / (float) steps;
                buffer.addVertex(pose, lastSin * 0.2F, lastCos * 0.2F, 0.0F)
                        .setColor(beamColorFade)
                        .setUv(lastU, v0)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(lightCoords)
                        .setNormal(pose, 0.0F, -1.0F, 0.0F);
                buffer.addVertex(pose, lastSin, lastCos, length)
                        .setColor(beamColor)
                        .setUv(lastU, v1)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(lightCoords)
                        .setNormal(pose, 0.0F, -1.0F, 0.0F);
                buffer.addVertex(pose, sin, cos, length)
                        .setColor(beamColor)
                        .setUv(u, v1)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(lightCoords)
                        .setNormal(pose, 0.0F, -1.0F, 0.0F);
                buffer.addVertex(pose, sin * 0.2F, cos * 0.2F, 0.0F)
                        .setColor(beamColorFade)
                        .setUv(u, v0)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(lightCoords)
                        .setNormal(pose, 0.0F, -1.0F, 0.0F);
                lastSin = sin;
                lastCos = cos;
                lastU = u;
            }
        });
        poseStack.popPose();
    }

    /**
     * Sets the current beam color on the render state.
     */
    @Inject(
            method =
                    "extractRenderState(Lnet/minecraft/world/entity/boss/enderdragon/EndCrystal;Lnet/minecraft/client/renderer/entity/state/EndCrystalRenderState;F)V",
            at = @At("RETURN"))
    public void includeBeamInformation(
            EndCrystal endCrystal, EndCrystalRenderState endCrystalRenderState, float f, CallbackInfo ci) {
        var color = endCrystal
                .noxesium$getOptionalComponent(CommonEntityComponentTypes.BEAM_COLOR)
                .map(Color::getRGB)
                .orElse(null);
        var fade = endCrystal
                .noxesium$getOptionalComponent(CommonEntityComponentTypes.BEAM_COLOR_FADE)
                .map(Color::getRGB)
                .orElse(null);
        endCrystalRenderState.noxesium$setBeamColor(color, fade);
    }
}
