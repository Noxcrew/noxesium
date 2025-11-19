package com.noxcrew.noxesium.mixin.rules.entity;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.noxcrew.noxesium.feature.entity.ExtraEntityData;
import java.awt.Color;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.client.renderer.entity.state.GuardianRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into the guardian rendering code to allow the color and alpha value of them to be changed.
 */
@Mixin(GuardianRenderer.class)
public class GuardianBeamColorRendererMixin {

    /**
     * Sets the current beam color on the render state.
     */
    @Inject(
            method =
                    "extractRenderState(Lnet/minecraft/world/entity/monster/Guardian;Lnet/minecraft/client/renderer/entity/state/GuardianRenderState;F)V",
            at = @At("RETURN"))
    public void includeBeamInformation(
            Guardian guardian, GuardianRenderState guardianRenderState, float f, CallbackInfo ci) {
        var color = guardian.noxesium$getExtraData(ExtraEntityData.BEAM_COLOR)
                .map(Color::getRGB)
                .orElse(null);
        var fade = guardian.noxesium$getExtraData(ExtraEntityData.BEAM_COLOR_FADE)
                .map(Color::getRGB)
                .orElse(null);
        guardianRenderState.noxesium$setBeamColor(color, fade);
    }

    /**
     * Swap out the type of rendering for a transparent one so we can properly make it transparent.
     */
    @Redirect(
            method = "<clinit>",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/rendertype/RenderTypes;entityCutoutNoCull(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private static RenderType determineRenderType(Identifier Identifier) {
        return RenderTypes.entityTranslucent(Identifier);
    }

    /**
     * Override the custom geometry code to recolor the beacon beam.
     */
    @Redirect(
            method =
                    "submit(Lnet/minecraft/client/renderer/entity/state/GuardianRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/entity/GuardianRenderer;renderBeam(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/world/phys/Vec3;FFF)V"))
    private void submit(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            Vec3 beamVector,
            float timeInTicks,
            float scale,
            float texVOff,
            @Local(argsOnly = true) GuardianRenderState guardianRenderState) {
        /*
         * Unfortunately because this uses a custom geometry we have to override the entire
         * method and redo all of this logic to change the colors. :/
         */
        float length = (float) (beamVector.length() + 1.0D);
        beamVector = beamVector.normalize();
        float xRot = (float) Math.acos(beamVector.y);
        float yRot = 1.5707964F - (float) Math.atan2(beamVector.z, beamVector.x);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot * 57.295776F));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot * 57.295776F));
        float rot = timeInTicks * 0.05F * -1.5F;
        float colorScale = scale * scale;
        var beamColor = guardianRenderState.noxesium$getBeamColor();
        int red = beamColor == null ? 64 + (int) (colorScale * 191.0F) : ARGB.red(beamColor);
        int green = beamColor == null ? 32 + (int) (colorScale * 191.0F) : ARGB.green(beamColor);
        int blue = beamColor == null ? 128 - (int) (colorScale * 64.0F) : ARGB.blue(beamColor);
        int alpha = beamColor == null ? 256 : ARGB.alpha(beamColor);
        float rr1 = 0.2F;
        float rr2 = 0.282F;
        float wnx = Mth.cos((rot + 2.3561945F)) * rr2;
        float wnz = Mth.sin((rot + 2.3561945F)) * rr2;
        float enx = Mth.cos((rot + 0.7853982F)) * rr2;
        float enz = Mth.sin((rot + 0.7853982F)) * rr2;
        float wsx = Mth.cos((rot + 3.926991F)) * rr2;
        float wsz = Mth.sin((rot + 3.926991F)) * rr2;
        float esx = Mth.cos((rot + 5.4977875F)) * rr2;
        float esz = Mth.sin((rot + 5.4977875F)) * rr2;
        float wx = Mth.cos((rot + 3.1415927F)) * rr1;
        float wz = Mth.sin((rot + 3.1415927F)) * rr1;
        float ex = Mth.cos((rot + 0.0F)) * rr1;
        float ez = Mth.sin((rot + 0.0F)) * rr1;
        float nx = Mth.cos((rot + 1.5707964F)) * rr1;
        float nz = Mth.sin((rot + 1.5707964F)) * rr1;
        float sx = Mth.cos((rot + 4.712389F)) * rr1;
        float sz = Mth.sin((rot + 4.712389F)) * rr1;
        float top = length;
        float minU = 0.0F;
        float maxU = 0.4999F;
        float minV = -1.0F + texVOff;
        float maxV = minV + length * 2.5F;
        var beamColorFade = guardianRenderState.noxesium$getBeamColorFade();
        int redFade = beamColorFade == null ? red : ARGB.red(beamColorFade);
        int greenFade = beamColorFade == null ? green : ARGB.green(beamColorFade);
        int blueFade = beamColorFade == null ? blue : ARGB.blue(beamColorFade);
        int alphaFade = beamColorFade == null ? alpha : ARGB.alpha(beamColorFade);

        submitNodeCollector.submitCustomGeometry(poseStack, GuardianRenderer.BEAM_RENDER_TYPE, (pose, buffer) -> {
            noxesium$vertex(buffer, pose, wx, top, wz, redFade, greenFade, blueFade, alphaFade, maxU, maxV);
            noxesium$vertex(buffer, pose, wx, 0.0F, wz, red, green, blue, alpha, maxU, minV);
            noxesium$vertex(buffer, pose, ex, 0.0F, ez, red, green, blue, alpha, minU, minV);
            noxesium$vertex(buffer, pose, ex, top, ez, redFade, greenFade, blueFade, alphaFade, minU, maxV);
            noxesium$vertex(buffer, pose, nx, top, nz, redFade, greenFade, blueFade, alphaFade, maxU, maxV);
            noxesium$vertex(buffer, pose, nx, 0.0F, nz, red, green, blue, alpha, maxU, minV);
            noxesium$vertex(buffer, pose, sx, 0.0F, sz, red, green, blue, alpha, minU, minV);
            noxesium$vertex(buffer, pose, sx, top, sz, redFade, greenFade, blueFade, alphaFade, minU, maxV);

            float vBase = (Mth.floor(timeInTicks) % 2 == 0) ? 0.5F : 0.0F;
            noxesium$vertex(buffer, pose, wnx, top, wnz, redFade, greenFade, blueFade, alphaFade, 0.5F, vBase + 0.5F);
            noxesium$vertex(buffer, pose, enx, top, enz, redFade, greenFade, blueFade, alphaFade, 1.0F, vBase + 0.5F);
            noxesium$vertex(buffer, pose, esx, top, esz, redFade, greenFade, blueFade, alphaFade, 1.0F, vBase);
            noxesium$vertex(buffer, pose, wsx, top, wsz, redFade, greenFade, blueFade, alphaFade, 0.5F, vBase);
        });
    }

    /**
     *  Adds a vertex to the builder for the beacon beam.
     */
    @Unique
    private static void noxesium$vertex(
            VertexConsumer builder,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            int red,
            int green,
            int blue,
            int alpha,
            float u,
            float v) {
        builder.addVertex(pose, x, y, z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
