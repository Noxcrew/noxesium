package com.noxcrew.noxesium.core.fabric.mixin.feature.sound;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.core.fabric.feature.sound.NoxesiumSoundInstance;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds support for the start offset of Noxesium sounds.
 */
@Mixin(SoundEngine.class)
public abstract class CustomNoxesiumSoundsMixin {

    @WrapOperation(
            method = "play",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/sounds/SoundBufferLibrary;getStream(Lnet/minecraft/resources/Identifier;Z)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<AudioStream> advanceNoxesiumStream(
            SoundBufferLibrary library,
            Identifier path,
            boolean looping,
            Operation<CompletableFuture<AudioStream>> original,
            @Local(argsOnly = true) SoundInstance soundInstance) {
        var future = original.call(library, path, looping);
        if (!(soundInstance instanceof NoxesiumSoundInstance noxesiumSoundInstance)) return future;

        var seconds = (int) Math.floor(noxesiumSoundInstance.getStartOffset());
        if (seconds <= 0) return future;

        return future.thenApply(audioStream -> {
            var bufferSize = ChannelExt.invokeCalculateBufferSize(audioStream.getFormat(), 1);
            try {
                audioStream.read(seconds * bufferSize);
            } catch (IOException e) {
                NoxesiumApi.getLogger().warn("Failed to seek sound {} to {}s", path, seconds, e);
            }
            return audioStream;
        });
    }

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
            @Local ChannelAccess.ChannelHandle channelHandle) {
        if (!(soundInstance instanceof NoxesiumSoundInstance noxesiumSoundInstance)) return;
        if (noxesiumSoundInstance.getStartOffset() <= 0) return;

        var sound = soundInstance.getSound();
        if (sound == null) return;

        var bufferedSeconds = !sound.shouldStream() ? 0 : (int) Math.floor(noxesiumSoundInstance.getStartOffset());
        noxesiumSoundInstance.applyStartOffset(channelHandle, bufferedSeconds);
    }
}
