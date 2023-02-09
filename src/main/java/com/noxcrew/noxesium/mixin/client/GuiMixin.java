package com.noxcrew.noxesium.mixin.client;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Raises the height of the held item name in the HUD to avoid
 * faction icons overlapping.
 */
@Mixin(Gui.class)
public class GuiMixin {

    private static final int ADJUST_BY = 5;

    @ModifyConstant(
            method = "renderSelectedItemName(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            constant = @Constant(intValue = 59)
    )
    public int modify(int constant) {
        return constant + ADJUST_BY;
    }
}
