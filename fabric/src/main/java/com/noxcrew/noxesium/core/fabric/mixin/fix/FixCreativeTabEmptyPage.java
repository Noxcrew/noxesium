package com.noxcrew.noxesium.core.fabric.mixin.fix;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fabricmc.fabric.api.client.itemgroup.v1.FabricCreativeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fixes a bug in Fabric's item group API where the current page isn't updated properly if there
 * are no eligible menus anymore.
 */
@Mixin(CreativeModeInventoryScreen.class)
public class FixCreativeTabEmptyPage {
    // This targets a method on FabricCreativeInventoryScreen!
    @WrapMethod(method = "getPage", remap = false)
    public int getPage(CreativeModeTab itemGroup, Operation<Integer> original) {
        var fabricScreen = (FabricCreativeInventoryScreen) (Object) this;
        return Math.clamp(original.call(itemGroup), 0, fabricScreen.getPageCount() - 1);
    }
}
