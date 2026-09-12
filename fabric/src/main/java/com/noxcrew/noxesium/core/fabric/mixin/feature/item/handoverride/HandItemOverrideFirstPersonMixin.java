package com.noxcrew.noxesium.core.fabric.mixin.feature.item.handoverride;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.core.fabric.util.InventoryHelper;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Overrides the item shown as being held in the main hand.
 * <p>
 * Applied at low priority so it modifies the main hand item before other mods such as Axiom who
 * want to clear it.
 */
@Mixin(value = FirstPersonHandsAndItems.class, priority = -2000)
public class HandItemOverrideFirstPersonMixin {
    @WrapOperation(
            method = "evaluateWhichHandsToRender",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack getSelected(LocalPlayer instance, Operation<ItemStack> original) {
        return InventoryHelper.getRealSelected(instance.getInventory());
    }
}
