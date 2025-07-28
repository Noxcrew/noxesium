package com.noxcrew.noxesium.mixin.sound;

import com.llamalad7.mixinextras.sugar.Local;
import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundInstance;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Overrides functionality of the sound engine to allow music-type tracks to continue playing even if they are muted.
 * This allows us to properly continue tracks where they left off if the user is tweaking their settings and temporarily
 * disables a track.
 */
@Mixin(SoundEngine.class)
public abstract class CustomNoxesiumSoundsMixin {

    @Shadow
    @Final
    private SoundBufferLibrary soundBuffers;

    @Shadow
    @Final
    private List<TickableSoundInstance> tickingSounds;

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
            @Local Sound sound,
            @Local(ordinal = 1) boolean isLooping,
            @Local(ordinal = 2) boolean streaming,
            @Local ChannelAccess.ChannelHandle channelHandle) {
        if (!(soundInstance instanceof NoxesiumSoundInstance noxesiumSoundInstance)) return;
        if (noxesiumSoundInstance.getStartOffset() <= 0) return;

        noxesiumSoundInstance.applyStartOffset(channelHandle);
        if (streaming) {
            ci.cancel();
            this.soundBuffers
                    .getStream(sound.getPath(), isLooping)
                    .thenAccept(audioStream -> channelHandle.execute(channel -> {
                        // Preloads `startOffset` amount of buffers but never reads them, so when minecraft loads
                        // the first 4 buffers, it's already offset by our `startOffset`
                        int bufferSize = ChannelExt.invokeCalculateBufferSize(audioStream.getFormat(), 1);
                        int startOffset = Mth.floor(noxesiumSoundInstance.getStartOffset());
                        try {
                            audioStream.read(startOffset * bufferSize);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        channel.attachBufferStream(audioStream);
                        channel.play();
                    }));
            this.tickingSounds.add((TickableSoundInstance) soundInstance);
        }
    }
}
