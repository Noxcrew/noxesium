package com.noxcrew.noxesium.mixin.rules;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Overrides [getSelected] with some item decided by the server whenever the player is not holding anything.
 * This allows the server to define CanPlace/CanBreak values or set custom tool properties on the empty hand.
 */
@Mixin(Inventory.class)
public abstract class HandItemOverrideInventoryMixin {

    @Shadow
    @Final
    public NonNullList<ItemStack> items;

    @Shadow
    public int selected;

    @ModifyReturnValue(method = "getSelected", at = @At("RETURN"))
    private ItemStack modifySelectedItem(ItemStack original) {
        if (original.isEmpty()) {
            var result = ServerRules.HAND_ITEM_OVERRIDE.getValue();

            // Return the original value as Mojang does a ==
            // comparison in specifically the attack logic!
            if (result.isEmpty()) return original;
            return result;
        }
        return original;
    }

    @ModifyReturnValue(method = "getDestroySpeed", at = @At("RETURN"))
    private float modifyDestroySpeed(float original, BlockState blockState) {
        var item = this.items.get(this.selected);
        if (item.isEmpty()) {
            return ServerRules.HAND_ITEM_OVERRIDE.getValue().getDestroySpeed(blockState);
        }
        return original;
    }
}
