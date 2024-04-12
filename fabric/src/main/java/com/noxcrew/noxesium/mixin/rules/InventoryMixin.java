package com.noxcrew.noxesium.mixin.rules;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Overrides [getSelected] with some item decided by the server whenever the player is not holding anything.
 * This allows the server to define CanPlace/CanBreak values or set custom tool properties on the empty hand.
 */
@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @ModifyReturnValue(method = "getSelected", at = @At("RETURN"))
    private ItemStack modifySelectedItem(ItemStack original) {
        if (original.isEmpty()) {
            return ServerRules.HAND_ITEM_OVERRIDE.getValue();
        }
        return original;
    }
}
