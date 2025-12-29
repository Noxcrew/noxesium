package com.noxcrew.noxesium.core.fabric.mixin.feature.sound;

import com.llamalad7.mixinextras.sugar.Local;
import com.noxcrew.noxesium.core.fabric.feature.sound.NoxesiumSoundInstance;
import java.io.IOException;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds support for the start offset of Noxesium sounds.
 */
@Mixin(SoundEngine.class)
public abstract class CustomNoxesiumSoundsMixin {

    @Shadow
    @Final
    private SoundBufferLibrary soundBuffers;

    @Inject(
            method = "play",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V",
                            shift = At.Shift.AFTER),
            cancellable = true)
    private void handleNoxesiumSounds(
            SoundInstance soundInstance,
            CallbackInfoReturnable<SoundEngine.PlayResult> ci,
            @Local ChannelAccess.ChannelHandle channelHandle) {
        if (!(soundInstance instanceof NoxesiumSoundInstance noxesiumSoundInstance)) return;
        if (noxesiumSoundInstance.getStartOffset() <= 0) return;

        // If this is a streaming source we need to forward the input stream by consuming
        // the start of it!
        var sound = soundInstance.getSound();
        if (sound == null) return;
        var isLooping = soundInstance.isLooping();
        var secondsToFormat = !sound.shouldStream() ? 0 : (int) Math.floor(noxesiumSoundInstance.getStartOffset());
        if (secondsToFormat > 0) {
            ci.cancel();
            soundBuffers
                    .getStream(sound.getPath(), isLooping)
                    .thenAccept(audioStream -> channelHandle.execute(channel -> {
                        // Preloads `startOffset` amount of buffers but never reads them,
                        // so when minecraft loads the first 4 buffers, it's already offset
                        // by our `startOffset`
                        int bufferSize = ChannelExt.invokeCalculateBufferSize(audioStream.getFormat(), 1);
                        try {
                            audioStream.read(secondsToFormat * bufferSize);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        channel.attachBufferStream(audioStream);
                        channel.play();
                    }));
        }

        // Non-streaming sounds can easily be offset
        noxesiumSoundInstance.applyStartOffset(channelHandle, secondsToFormat);
    }
}
