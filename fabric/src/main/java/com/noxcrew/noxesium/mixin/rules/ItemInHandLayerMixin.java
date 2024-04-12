package com.noxcrew.noxesium.mixin.rules;

import com.noxcrew.noxesium.feature.rule.InventoryHelper;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Overrides the item shown as being held in the main hand on the 3d player model.
 */
@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {

    @Redirect(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack getSelected(LivingEntity instance) {
        if (instance instanceof Player player) {
            return InventoryHelper.getRealSelected(player.getInventory());
        } else {
            return instance.getMainHandItem();
        }
    }
}
