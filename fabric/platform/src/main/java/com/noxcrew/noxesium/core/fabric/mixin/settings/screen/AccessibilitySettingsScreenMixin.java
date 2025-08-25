package com.noxcrew.noxesium.core.fabric.mixin.settings.screen;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.noxcrew.noxesium.core.fabric.config.VanillaOptions;
import java.util.List;
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
        var newOptions = List.of(
                VanillaOptions.resetToggleKeys(),
                VanillaOptions.renderMapsAsUi(),
                VanillaOptions.mapUiSize(),
                VanillaOptions.mapUiLocation());
        var newArray = new OptionInstance<?>[original.length + newOptions.size()];
        System.arraycopy(original, 0, newArray, 0, original.length);
        for (int i = newOptions.size(); i > 0; i--) {
            newArray[newArray.length - i] = newOptions.get(newOptions.size() - i);
        }
        return newArray;
    }
}
