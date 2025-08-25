package com.noxcrew.noxesium.core.fabric.mixin.sound;

import com.llamalad7.mixinextras.sugar.Local;
import com.noxcrew.noxesium.core.fabric.feature.sounds.NoxesiumSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds support for the start offset of Noxesium sounds.
 */
@Mixin(SoundEngine.class)
public abstract class CustomNoxesiumSoundsMixin {

    @Inject(
            method = "play",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V",
                            shift = At.Shift.AFTER))
    private void handleNoxesiumSounds(
            SoundInstance soundInstance,
            CallbackInfoReturnable<SoundEngine.PlayResult> ci,
            @Local Sound sound,
            @Local(ordinal = 1) boolean isLooping,
            @Local(ordinal = 2) boolean streaming,
            @Local ChannelAccess.ChannelHandle channelHandle) {
        if (!(soundInstance instanceof NoxesiumSoundInstance noxesiumSoundInstance)) return;
        if (streaming) return;
        if (noxesiumSoundInstance.getStartOffset() <= 0) return;
        noxesiumSoundInstance.applyStartOffset(channelHandle);
    }
}
