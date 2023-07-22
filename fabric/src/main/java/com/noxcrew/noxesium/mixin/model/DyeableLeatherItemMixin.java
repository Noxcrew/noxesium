package com.noxcrew.noxesium.mixin.model;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(DyeableLeatherItem.class)
public interface DyeableLeatherItemMixin {

    /**
     * @author Aeltumn
     * @reason Optimize NBT lookup, particularly effective when using lots of colored models
     */
    @Overwrite
    public default int getColor(ItemStack item) {
        final int defaultColor = 10511680;
        var itemTag = item.getTag();
        if (itemTag == null) return defaultColor;
        var tag = itemTag.get("display");

        // We check if the id is 10 which is a compound tag
        if (tag == null || tag.getId() != 10) return defaultColor;
        var compoundTag = (CompoundTag) tag;
        var color = compoundTag.get("color");

        // This is equal to a 99 contains check, effectively if the id is not one of the first 6
        // it's not a numeric tag
        if (color == null || color.getId() > 6) return defaultColor;
        var numericTag = (NumericTag) color;
        return numericTag.getAsInt();
    }
}
