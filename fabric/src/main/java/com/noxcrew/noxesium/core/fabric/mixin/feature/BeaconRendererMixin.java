package com.noxcrew.noxesium.core.fabric.mixin.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.noxcrew.noxesium.core.registry.CommonBlockEntityComponentTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Swaps out the beacon beam height with the custom component override.
 */
@Mixin(BeaconRenderer.class)
public class BeaconRendererMixin {

    @WrapOperation(
            method = "render",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/blockentity/BeaconRenderer;renderBeaconBeam(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;FFJIII)V"))
    private void render(
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            float partialTicks,
            float width,
            long gameTime,
            int y,
            int height,
            int color,
            Operation<Void> original,
            @Local(argsOnly = true) BlockEntity blockEntity) {
        var override = blockEntity.noxesium$getComponent(CommonBlockEntityComponentTypes.BEACON_BEAM_HEIGHT);
        if (override != null) {
            // Determine the maximum value this section can have
            var limit = Math.max(override + 1 - y, 0);
            height = Math.min(height, limit);

            // Don't render invisible sections!
            if (height == 0) return;
        }
        original.call(poseStack, multiBufferSource, partialTicks, width, gameTime, y, height, color);
    }
}
