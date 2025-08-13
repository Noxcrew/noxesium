package com.noxcrew.noxesium.core.fabric.mixin.feature;

import com.llamalad7.mixinextras.sugar.Local;
import com.noxcrew.noxesium.core.fabric.registry.CommonBlockEntityComponentTypes;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Swaps out the beacon beam height with the custom component override.
 */
@Mixin(BeaconRenderer.class)
public class BeaconRendererMixin {

    @ModifyConstant(method = "render", constant = @Constant(intValue = 2048))
    private int render(int constant, @Local(argsOnly = true) BlockEntity blockEntity) {
        var override = blockEntity.noxesium$getComponent(CommonBlockEntityComponentTypes.BEACON_BEAM_HEIGHT);
        if (override != null) {
            return override;
        }
        return 2048;
    }
}
