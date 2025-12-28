package com.noxcrew.noxesium.core.fabric.mixin.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.noxcrew.noxesium.core.registry.CommonBlockEntityComponentTypes;
import java.util.ArrayList;
import java.util.List;
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
public class BeaconRendererMixin {

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
        var override = blockEntity.noxesium$getComponent(CommonBlockEntityComponentTypes.BEACON_BEAM_HEIGHT);
        if (override != null) {
            // Modify the section list to ignore any sections above the height of the beacon beam
            var y = 0;
            var sections = new ArrayList<BeaconRenderState.Section>();
            for (var section : blockEntity.getBeamSections()) {
                var limit = Math.max(override + 1 - y, 0);
                var height = Math.min(section.getHeight(), limit);
                if (height > 0) {
                    var state = new BeaconRenderState.Section(section.getColor(), section.getHeight());
                    sections.add(state);
                    y += height;
                }
            }
            original.call(instance, sections);
        } else {
            original.call(instance, value);
        }
    }
}
