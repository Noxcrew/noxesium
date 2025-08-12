package com.noxcrew.noxesium.fabric.mixin.feature;

import com.noxcrew.noxesium.fabric.feature.misc.ImmovableTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for preventing items from being moved inside custom inventories.
 */
@Mixin(Slot.class)
public abstract class UnmovableSlotMixin {

    @Shadow
    public abstract ItemStack getItem();

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    public void preventMovingImmovables(final Player player, final CallbackInfoReturnable<Boolean> cir) {
        if (ImmovableTag.isImmovable(getItem())) {
            cir.setReturnValue(false);
        }
    }
}
