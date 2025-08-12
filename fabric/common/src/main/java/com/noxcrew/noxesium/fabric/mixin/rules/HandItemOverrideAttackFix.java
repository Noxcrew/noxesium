package com.noxcrew.noxesium.fabric.mixin.rules;

import com.noxcrew.noxesium.fabric.feature.rule.InventoryHelper;
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

    @Redirect(
            method = "attack",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack redirectGetMainHandItem(Player instance) {
        // Specifically get the true internal selected slot!
        return InventoryHelper.getRealSelected(instance.getInventory());
    }
}
