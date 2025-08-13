package com.noxcrew.noxesium.fabric.mixin.rules;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.noxcrew.noxesium.fabric.registry.CommonGameComponentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Overrides [getSelected] with some item decided by the server whenever the player is not holding anything.
 * This allows the server to define CanPlace/CanBreak values or set custom tool properties on the empty hand.
 */
@Mixin(Inventory.class)
public abstract class HandItemOverrideInventoryMixin {

    @ModifyReturnValue(method = "getSelectedItem", at = @At("RETURN"))
    private ItemStack modifySelectedItem(ItemStack original) {
        if (original.isEmpty()) {
            // Return the original value if it's empty anyway so it matches any == comparisons!
            var result = Minecraft.getInstance()
                    .noxesium$getComponentOr(CommonGameComponentTypes.HAND_ITEM_OVERRIDE, () -> ItemStack.EMPTY);
            if (result.isEmpty()) return original;
            return result;
        }
        return original;
    }
}
