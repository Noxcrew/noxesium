package com.noxcrew.noxesium.fabric.mixin.feature;

import com.llamalad7.mixinextras.sugar.Local;
import com.noxcrew.noxesium.fabric.feature.misc.ImmovableTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for preventing items dropped from the hotbar.
 */
@Mixin(Inventory.class)
public abstract class UnmovableInventoryMixin {

    @Inject(
            method = "removeFromSelected",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"),
            cancellable = true)
    public void preventMovingImmovables(
            final boolean bl, final CallbackInfoReturnable<ItemStack> cir, @Local final ItemStack stack) {
        if (ImmovableTag.isImmovable(stack)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
