package com.noxcrew.noxesium.core.fabric.mixin.rules;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.core.fabric.util.InventoryHelper;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Overrides the item shown as being held in the main hand on the 3d player model.
 */
@Mixin(ArmedEntityRenderState.class)
public abstract class HandItemOverrideRenderMixin {

    @WrapOperation(
            method = "extractArmedEntityRenderState",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/LivingEntity;getItemHeldByArm(Lnet/minecraft/world/entity/HumanoidArm;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack getItemByArm(LivingEntity instance, HumanoidArm arm, Operation<ItemStack> original) {
        if (instance instanceof Player player && arm == player.getMainArm()) {
            return InventoryHelper.getRealSelected(player.getInventory());
        } else {
            return instance.getItemHeldByArm(arm);
        }
    }
}
