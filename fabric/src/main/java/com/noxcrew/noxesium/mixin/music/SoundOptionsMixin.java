package com.noxcrew.noxesium.mixin.music;

import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

/**
 * Hides the additional music sliders when the system is not active.
 */
@Mixin(SoundOptionsScreen.class)
public class SoundOptionsMixin {

    @Inject(method = "getAllSoundOptionsExceptMaster", at = @At("RETURN"), cancellable = true)
    private void injected(CallbackInfoReturnable<OptionInstance<?>[]> cir) {
        if (ServerRules.ENABLE_CUSTOM_MUSIC.get()) return;
        var options = Minecraft.getInstance().options;
        cir.setReturnValue(
                Arrays.stream(SoundSource.values())
                        // Filter out the two custom music sliders
                        .filter((soundSource) -> soundSource != SoundSource.MASTER && !soundSource.getName().equals("core_music") && !soundSource.getName().equals("game_music"))
                        .map(options::getSoundSourceOptionInstance)
                        .toArray(OptionInstance[]::new)
        );
    }
}
