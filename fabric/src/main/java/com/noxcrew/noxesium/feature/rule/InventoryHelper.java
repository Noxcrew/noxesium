package com.noxcrew.noxesium.feature.rule;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Helps with figuring out the currently selected item. */
public class InventoryHelper {

    /** Returns the real item selected in [inventory], which may differ from the server override. */
    public static ItemStack getRealSelected(Inventory inventory) {
        return Inventory.isHotbarSlot(inventory.selected) ? inventory.items.get(inventory.selected) : ItemStack.EMPTY;
    }
}
