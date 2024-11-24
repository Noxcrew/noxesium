package com.noxcrew.noxesium.mixin.rules;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fixes a specific issue in how the hand item override interacts with the
 * attack() function logic.
 */
@Mixin(Player.class)
public class HandItemOverrideAttackFix {

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack redirectGetMainHandItem(Player instance) {
        // Specifically get the true internal selected slot!
        var inventory = instance.getInventory();
        return Inventory.isHotbarSlot(inventory.selected) ? inventory.items.get(inventory.selected) : ItemStack.EMPTY;
    }
}
