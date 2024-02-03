package com.noxcrew.noxesium.mixin.performance.model;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.item.DyeableLeatherItem.DEFAULT_LEATHER_COLOR;

@Mixin(DyeableLeatherItem.class)
public interface DyeableLeatherItemMixin {

    /**
     * @author Aeltumn
     * @reason Optimize NBT lookup, particularly effective when using lots of colored models
     */
    @Inject(method = "getColor", at = @At("HEAD"), cancellable = true)
    default void getColorOptimized(ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
        var itemTag = itemStack.getTag();
        if (itemTag == null) {
            cir.setReturnValue(DEFAULT_LEATHER_COLOR);
            return;
        }
        var tag = itemTag.get("display");

        // We check if the id is 10 which is a compound tag
        if (tag == null || tag.getId() != 10) {
            cir.setReturnValue(DEFAULT_LEATHER_COLOR);
            return;
        }
        var compoundTag = (CompoundTag) tag;
        var color = compoundTag.get("color");

        // This is equal to a 99 contains check, effectively if the id is not one of the first 6
        // it's not a numeric tag
        if (color == null || color.getId() > 6) {
            cir.setReturnValue(DEFAULT_LEATHER_COLOR);
            return;
        }
        var numericTag = (NumericTag) color;
        cir.setReturnValue(numericTag.getAsInt());
    }
}
