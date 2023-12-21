package com.noxcrew.noxesium.mixin.rules.adventure;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Overrides [ItemStack#useOn] to support global can place on.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hasAdventureModePlaceTagForBlock(Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/block/state/pattern/BlockInWorld;)Z"))
    private boolean checkIfServerAllowsItemUsage(ItemStack instance, Registry<Block> registry, BlockInWorld blockInWorld, Operation<Boolean> original) {
        if (original.call(instance, registry, blockInWorld)) {
            return true;
        }

        // Also allow usage when the global can place on allows it
        return ServerRules.GLOBAL_CAN_PLACE_ON.getValue().test(registry, blockInWorld);
    }
}
