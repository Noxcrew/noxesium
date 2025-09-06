package com.noxcrew.noxesium.core.fabric.mixin.rules.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.noxcrew.noxesium.core.fabric.feature.entity.EndCrystalRenderHolder;
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes;
import java.awt.Color;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into the end crystal rendering.
 */
@Mixin(EndCrystalRenderer.class)
public class EndCrystalRendererMixin {

    /**
     * Prepare the color when we begin rendering.
     */
    @Inject(
            method =
                    "render(Lnet/minecraft/client/renderer/entity/state/EndCrystalRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void prepareColor(
            EndCrystalRenderState endCrystalRenderState,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int i,
            CallbackInfo ci) {
        EndCrystalRenderHolder.noxesium$endCrystalBeamColor = endCrystalRenderState.noxesium$getBeamColor();
        EndCrystalRenderHolder.noxesium$endCrystalBeamColorFade = endCrystalRenderState.noxesium$getBeamColorFade();
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
