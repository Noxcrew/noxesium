package com.noxcrew.noxesium.mixin.rules;

import com.noxcrew.noxesium.feature.rule.InventoryHelper;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Overrides the item shown as being held in the main hand on the 3d player model.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class HandItemOverrideRenderMixin {

    @Redirect(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemHeldByArm(Lnet/minecraft/world/entity/HumanoidArm;)Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack getItemByArm(LivingEntity instance, HumanoidArm humanoidArm) {
        if (instance instanceof Player player && humanoidArm == player.getMainArm()) {
            return InventoryHelper.getRealSelected(player.getInventory());
        } else {
            return instance.getItemHeldByArm(humanoidArm);
        }
    }
}
