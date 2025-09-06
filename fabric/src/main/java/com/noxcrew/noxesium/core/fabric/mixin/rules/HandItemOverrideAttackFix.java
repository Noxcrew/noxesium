package com.noxcrew.noxesium.core.fabric.mixin.rules;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.core.fabric.util.InventoryHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fixes a specific issue in how the hand item override interacts with the
 * attack() function logic.
 */
@Mixin(Player.class)
public class HandItemOverrideAttackFix {

    @WrapOperation(
            method = "attack",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack redirectGetMainHandItem(Player instance, Operation<ItemStack> original) {
        // Specifically get the true internal selected slot!
        return InventoryHelper.getRealSelected(instance.getInventory());
    }
}
