package com.noxcrew.noxesium.mixin.sound;

import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Overrides functionality of the sound engine to allow music-type tracks to continue playing even if they are muted.
 * This allows us to properly continue tracks where they left off if the user is tweaking their settings and temporarily
 * disables a track.
 */
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

    // TODO We can consider pausing the channel for a muted sound but it's TBD if there's any performance gain there, who turns off audio for fps anyway?

    @Shadow
    protected abstract float calculateVolume(SoundInstance soundInstance);

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
        var volume = instance.getSoundSourceVolume(soundSource);
        return volume <= 0.0F && soundSource.getName().contains("music") ? 1.0f : volume;
    }
}
