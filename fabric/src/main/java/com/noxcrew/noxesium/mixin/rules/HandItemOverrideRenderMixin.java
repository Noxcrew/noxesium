package com.noxcrew.noxesium.mixin.rules;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.feature.rule.InventoryHelper;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Overrides the item shown as being held in the main hand on the 3d player model.
 */
@Mixin(ItemInHandLayer.class)
public abstract class HandItemOverrideRenderMixin {

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack getSelected(LivingEntity instance, Operation<ItemStack> original) {
        if (instance instanceof Player player) {
            return InventoryHelper.getRealSelected(player.getInventory());
        } else {
            return original.call(instance);
        }
    }
}
