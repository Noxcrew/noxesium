package com.noxcrew.noxesium.core.fabric.mixin.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fixes MC-577 by making inventory closing and drop item work when bound to mouse buttons.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class FixMouseInventoryKeys {

    @Shadow
    public abstract void onClose();

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Shadow
    protected abstract void slotClicked(Slot p_97778_, int p_97779_, int p_97780_, ClickType p_97781_);

    @Inject(
            method = "mouseClicked",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;getHoveredSlot(DD)Lnet/minecraft/world/inventory/Slot;",
                            shift = At.Shift.BEFORE),
            cancellable = true)
    public void onClickMouse(MouseButtonEvent mouseButtonEvent, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.options.keyInventory.matchesMouse(mouseButtonEvent)) {
            onClose();
            cir.setReturnValue(true);
        } else if (hoveredSlot != null
                && hoveredSlot.hasItem()
                && minecraft.options.keyDrop.matchesMouse(mouseButtonEvent)) {
            slotClicked(hoveredSlot, hoveredSlot.index, mouseButtonEvent.hasControlDown() ? 1 : 0, ClickType.THROW);
            cir.setReturnValue(false);
        }
    }
}
