package com.noxcrew.noxesium.core.fabric.mixin.rules;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.core.fabric.registry.CommonGameComponentTypes;
import com.noxcrew.noxesium.core.fabric.util.InventoryHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Raises the height of the held item name in the HUD to avoid
 * faction icons overlapping.
 * <p>
 * Also overrides the selected item for hover to ignore the rule override.
 */
@Mixin(Gui.class)
public abstract class GuiRulesMixin {

    @ModifyConstant(
            method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;)V",
            constant = @Constant(intValue = 59))
    public int raiseHeldItemHeight(int constant) {
        return constant
                + Minecraft.getInstance()
                        .noxesium$getComponentOr(CommonGameComponentTypes.HELD_ITEM_NAME_OFFSET, () -> 0);
    }

    @WrapOperation(
            method = "tick()V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/player/Inventory;getSelectedItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack getSelected(Inventory inventory, Operation<ItemStack> original) {
        return InventoryHelper.getRealSelected(inventory);
    }
}
