package com.noxcrew.noxesium.mixin.performance;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.config.VanillaOptions;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds Noxesium's custom settings to the vanilla video settings menu.
 */
@Mixin(VideoSettingsScreen.class)
public class VideoSettingsScreenMixin {

    @Inject(method = "options", at = @At("TAIL"), cancellable = true)
    private static void changeOptions(Options options, CallbackInfoReturnable<OptionInstance<?>[]> cir) {
        var oldArray = cir.getReturnValue();
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
