package com.noxcrew.noxesium.mixin.rules;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.feature.rule.InventoryHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fixes a specific issue in how the hand item override interacts with the
 * attack() function logic.
 */
@Mixin(LivingEntity.class)
public abstract class HandItemOverrideAttackFix {

    @WrapOperation(
            method = "getWeaponItem",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/LivingEntity;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack redirectGetMainHandItem(LivingEntity instance, Operation<ItemStack> original) {
        if (instance instanceof Player player) {
            // Specifically get the true internal selected slot!
            return InventoryHelper.getRealSelected(player.getInventory());
        } else {
            return original.call(instance);
        }
    }
}
