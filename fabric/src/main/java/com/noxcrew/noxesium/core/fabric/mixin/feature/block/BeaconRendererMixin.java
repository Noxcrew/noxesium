package com.noxcrew.noxesium.core.fabric.mixin.feature.block;

import static net.minecraft.client.renderer.blockentity.BeaconRenderer.MAX_RENDER_Y;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.noxcrew.noxesium.core.registry.CommonBlockEntityComponentTypes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.state.BeaconRenderState;
import net.minecraft.world.level.block.entity.BeaconBeamOwner;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Swaps out the beacon beam height with the custom component override.
 */
@Mixin(BeaconRenderer.class)
public class BeaconRendererMixin<T extends BlockEntity & BeaconBeamOwner> {

    @WrapOperation(
            method = "extract",
            at =
                    @At(
                            value = "FIELD",
                            target =
                                    "Lnet/minecraft/client/renderer/blockentity/state/BeaconRenderState;sections:Ljava/util/List;",
                            opcode = Opcodes.PUTFIELD))
    private static <T extends BlockEntity & BeaconBeamOwner> void extract(
            BeaconRenderState instance,
            List<BeaconRenderState.Section> value,
            Operation<Void> original,
            @Local(argsOnly = true) T blockEntity) {
        // Determine the maximum height of the beacon beam
        var override = blockEntity.noxesium$getComponent(CommonBlockEntityComponentTypes.BEACON_BEAM_HEIGHT);
        var total = override == null ? MAX_RENDER_Y : override + 1;

        // Modify the section list to ignore any sections above the height
        // of the beacon beam and to ensure we get exactly to the height!
        var y = 0;
        var sections = new ArrayList<BeaconRenderState.Section>();
        for (var i = 0; i < value.size(); i++) {
            var section = value.get(i);
            var limit = Math.max(total - y, 0);

            // The last section should be the max height instead of the height it's set as which will always be 1
            var isLast = i == value.size() - 1;
            var height = Math.min(isLast ? MAX_RENDER_Y : section.height(), limit);
            if (height > 0) {
                var state = new BeaconRenderState.Section(section.color(), height);
                sections.add(state);
                y += height;
            } else {
                // Stop iterating if we've found an element we cannot add!
                break;
            }
        }
        original.call(instance, sections);
    }

    @WrapOperation(
            method =
                    "submit(Lnet/minecraft/client/renderer/blockentity/state/BeaconRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/blockentity/BeaconRenderer;submitBeaconBeam(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;FFIII)V"))
    private void submit(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            float beamRadiusScale,
            float animationTime,
            int startY,
            int height,
            int color,
            Operation<Void> original,
            @Local BeaconRenderState.Section section) {
        // Ignore the height passed to the submit method and use the section height directly!
        original.call(poseStack, submitNodeCollector, beamRadiusScale, animationTime, startY, section.height(), color);
    }
}
