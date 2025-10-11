package com.noxcrew.noxesium.feature;

import static com.noxcrew.noxesium.api.NoxesiumReferences.BUKKIT_COMPOUND_ID;
import static com.noxcrew.noxesium.api.NoxesiumReferences.IMMOVABLE_TAG;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

/**
 * Adds a helper method to look up if an item should be immovable.
 * Any items with the {@link com.noxcrew.noxesium.api.NoxesiumReferences#IMMOVABLE_TAG}
 * is marked as immovable.
 * <p>
 * The immovable tag can be used to improve using menus, as there won't be any
 * flickering when clicking on buttons anymore.
 */
public class ImmovableTag {
    /**
     * Returns whether the given item stack has the immovable tag.
     */
    public static boolean isImmovable(@Nullable ItemStack itemStack) {
        if (itemStack == null) return false;
        final CustomData data = itemStack.get(DataComponents.CUSTOM_DATA);
        if (data == null || data.isEmpty()) return false;
        final CompoundTag tag = data.copyTag();

        // If the immovable tag is directly in the tag we count it as immovable!
        if (tag.contains(IMMOVABLE_TAG)) return true;

        // Otherwise, we check if the tag exists inside the default Bukkit data block.
        final CompoundTag bukkit = tag.getCompound(BUKKIT_COMPOUND_ID).orElse(null);
        if (bukkit == null) return false;
        return bukkit.contains(IMMOVABLE_TAG);
    }
}
