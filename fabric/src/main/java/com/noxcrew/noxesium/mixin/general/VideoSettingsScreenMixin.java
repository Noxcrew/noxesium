package com.noxcrew.noxesium.mixin.general;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.config.VanillaOptions;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Adds Noxesium's custom settings to the vanilla video settings menu.
 */
@Mixin(VideoSettingsScreen.class)
public abstract class VideoSettingsScreenMixin {

    @ModifyReturnValue(method = "options", at = @At("TAIL"))
    private static OptionInstance<?>[] changeOptions(OptionInstance<?>[] original) {
        var showExperimentalPatches = NoxesiumMod.getInstance().getConfig().areExperimentalPatchesAvailable();
        var newArray = new OptionInstance<?>[original.length + (showExperimentalPatches ? 2 : 1)];
        System.arraycopy(original, 0, newArray, 0, original.length);
        if (showExperimentalPatches) {
            newArray[newArray.length - 2] = VanillaOptions.experimentalPatches();
        }
        newArray[newArray.length - 1] = VanillaOptions.fpsOverlay();
        return newArray;
    }
}
