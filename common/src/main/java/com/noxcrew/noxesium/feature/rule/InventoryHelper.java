package com.noxcrew.noxesium.feature.rule;

import com.noxcrew.noxesium.mixin.rules.ext.InventoryExt;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Helps with figuring out the currently selected item. */
public class InventoryHelper {

    /** Returns the real item selected in [inventory], which may differ from the server override. */
    public static ItemStack getRealSelected(Inventory inventory) {
        return Inventory.isHotbarSlot(inventory.getSelectedSlot())
                ? ((InventoryExt) inventory).getItems().get(inventory.getSelectedSlot())
                : ItemStack.EMPTY;
    }
}
