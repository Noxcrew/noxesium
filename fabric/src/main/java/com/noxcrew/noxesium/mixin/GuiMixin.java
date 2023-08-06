package com.noxcrew.noxesium.mixin;

import com.noxcrew.noxesium.feature.rule.ServerRules;
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

    @ModifyConstant(
            method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;)V",
            constant = @Constant(intValue = 59)
    )
    public int modify(int constant) {
        return constant + ServerRules.HELD_ITEM_NAME_OFFSET.getValue();
    }
}
