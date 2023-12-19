package com.noxcrew.noxesium.mixin.performance;

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
        var newArray = new OptionInstance<?>[oldArray.length + (showExperimentalPatches ? 2 : 1)];
        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
        if (showExperimentalPatches) {
            newArray[newArray.length - 2] = VanillaOptions.experimentalPatches();
        }
        newArray[newArray.length - 1] = VanillaOptions.fpsOverlay();
        cir.setReturnValue(newArray);
    }
}
