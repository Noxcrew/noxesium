package com.noxcrew.noxesium.mixin.sound;

import com.mojang.blaze3d.audio.Channel;
import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundInstance;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Overrides functionality of the sound engine to allow music-type tracks to continue playing even if they are muted.
 * This allows us to properly continue tracks where they left off if the user is tweaking their settings and temporarily
 * disables a track.
 */
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

    @Shadow
    protected abstract float calculateVolume(SoundInstance soundInstance);

    @Shadow
    @Final
    private SoundBufferLibrary soundBuffers;
    @Shadow
    @Final
    private List<TickableSoundInstance> tickingSounds;

    @Inject(method = "method_19754", at = @At("HEAD"), cancellable = true)
    public void updateCategoryVolume(SoundInstance soundInstance, ChannelAccess.ChannelHandle channelHandle, CallbackInfo ci) {
        ci.cancel();

        var volume = calculateVolume(soundInstance);
        channelHandle.execute((channel) -> {
            if (volume <= 0.0F && !soundInstance.getSource().getName().contains("music")) {
                channel.stop();
            } else {
                channel.setVolume(volume);
            }
        });
    }

    @Redirect(method = "tickNonPaused", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getSoundSourceVolume(Lnet/minecraft/sounds/SoundSource;)F"))
    private float overrideVolume(Options instance, SoundSource soundSource) {
        // We return a non-0.0 value if we don't want to stop playing the music to avoid it getting stopped.
        // We can consider pausing the channel for a muted sound, but it's TBD if there's any performance gain there, who turns off audio for fps anyway?
        var volume = instance.getSoundSourceVolume(soundSource);
        return volume <= 0.0F && soundSource.getName().contains("music") ? 1.0f : volume;
    }

    @Inject(
            method = "play",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void execute(SoundInstance soundInstance, CallbackInfo ci, WeighedSoundEvents weighedSoundEvents, ResourceLocation resourceLocation, Sound sound, float f, float g, SoundSource soundSource, float h, float i, SoundInstance.Attenuation attenuation, boolean bl, Vec3 vec3, boolean isLooping, boolean streaming, CompletableFuture completableFuture, ChannelAccess.ChannelHandle channelHandle) {
        if (!(soundInstance instanceof NoxesiumSoundInstance soundInstance1)) return;
        if (soundInstance1.getStartOffset() <= 0) return;

        soundInstance1.applyStartOffset(channelHandle);
        if (streaming) {
            ci.cancel();
            this.soundBuffers.getStream(sound.getPath(), isLooping).thenAccept(audioStream -> channelHandle.execute(channel -> {
                // Preloads `startOffset` amount of buffers but never reads them, so when minecraft loads
                // the first 4 buffers, it's already offset by our `startOffset`
                int bufferSize = ChannelExt.calculateBufferSize(audioStream.getFormat(), 1);
                int startOffset = Mth.floor(soundInstance1.getStartOffset());
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
