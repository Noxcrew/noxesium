package com.noxcrew.noxesium.mixin.feature;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.noxcrew.noxesium.api.NoxesiumReferences.BUKKIT_COMPOUND_ID;
import static com.noxcrew.noxesium.api.NoxesiumReferences.IMMOVABLE_TAG;

/**
 * Mixin for preventing items from being moved inside custom inventories.
 * This only prevent items with the {@link com.noxcrew.noxesium.api.NoxesiumReferences#IMMOVABLE_TAG} from being moved.
 * This is done to improve using menus, as there won't be any flickering when clicking on buttons anymore.
 */
@Mixin(Slot.class)
public abstract class UnmovableSlotMixin {

    @Shadow
    public abstract ItemStack getItem();

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    public void preventMovingImmovables(final Player player, final CallbackInfoReturnable<Boolean> cir) {
        final ItemStack itemStack = getItem();
        if (itemStack == null) return;

        final CustomData data = itemStack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return;
        final CompoundTag tag = data.getUnsafe();
        if (tag == null) return;

        final CompoundTag bukkit = tag.getCompound(BUKKIT_COMPOUND_ID);
        if (!bukkit.contains(IMMOVABLE_TAG)) return;

        cir.setReturnValue(false);
    }
}
