package com.noxcrew.noxesium.mixin.feature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class FixMovingPistonFlickeringMixin {

    @Shadow
    public abstract Block getBlock();

    @ModifyReturnValue(method = "isCollisionShapeFullBlock", at = @At("RETURN"))
    private boolean movingPistonsAreNotFullBlocks(boolean original) {
        // Enforce moving piston blocks to not have a full collision shape so
        // they don't cast shadows on neighboring blocks
        if (getBlock() instanceof MovingPistonBlock) return false;

        return original;
    }
}
