package com.noxcrew.noxesium.mixin.rules;

import com.noxcrew.noxesium.feature.rule.InventoryHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Overrides the item shown as being held in the main hand.
 */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack getSelected(LocalPlayer instance) {
        return InventoryHelper.getRealSelected(instance.getInventory());
    }
}
