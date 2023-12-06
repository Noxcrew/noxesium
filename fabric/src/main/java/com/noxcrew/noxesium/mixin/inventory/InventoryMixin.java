package com.noxcrew.noxesium.mixin.inventory;

import com.noxcrew.noxesium.NoxesiumMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.noxcrew.noxesium.NoxesiumMod.BUKKIT_COMPOUND_ID;
import static com.noxcrew.noxesium.NoxesiumMod.IMMOVABLE_TAG;

/**
 * Mixin for preventing items dropped from the hotbar.
 * This only prevent items with the {@link NoxesiumMod#IMMOVABLE_TAG} from being moved.
 */
@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @Inject(
            method = "removeFromSelected",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    public void removeFromSelected(final boolean bl, final CallbackInfoReturnable<ItemStack> cir, final ItemStack itemStack) {
        final CompoundTag tag = itemStack.getTag();
        if (tag == null) return;

        final CompoundTag bukkit = tag.getCompound(BUKKIT_COMPOUND_ID);
        if (!bukkit.contains(IMMOVABLE_TAG)) return;

        cir.setReturnValue(ItemStack.EMPTY);
    }
}
