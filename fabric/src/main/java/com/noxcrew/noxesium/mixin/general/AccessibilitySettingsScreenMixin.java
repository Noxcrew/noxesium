package com.noxcrew.noxesium.mixin.general;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.noxcrew.noxesium.config.VanillaOptions;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Adds Noxesium's custom settings to the vanilla accessibility settings menu.
 */
@Mixin(AccessibilityOptionsScreen.class)
public abstract class AccessibilitySettingsScreenMixin {

    @ModifyReturnValue(method = "options", at = @At("TAIL"))
    private static OptionInstance<?>[] changeOptions(OptionInstance<?>[] original) {
        var newArray = new OptionInstance<?>[original.length + 1];
        System.arraycopy(original, 0, newArray, 0, original.length);
        newArray[newArray.length - 1] = VanillaOptions.resetToggleKeys();
        return newArray;
    }
}
