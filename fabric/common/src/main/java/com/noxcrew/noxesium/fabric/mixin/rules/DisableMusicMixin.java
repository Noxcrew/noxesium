package com.noxcrew.noxesium.fabric.mixin.rules;

import com.noxcrew.noxesium.fabric.registry.CommonGameComponentTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into vanilla's music manager to fully disable vanilla music while its active.
 */
@Mixin(MusicManager.class)
public abstract class DisableMusicMixin {

    @Shadow
    @Nullable
    private SoundInstance currentMusic;

    @Shadow
    public abstract void stopPlaying();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo ci) {
        // Prevent vanilla music from ticking and starting to play whenever custom music is running
        if (Minecraft.getInstance().noxesium$hasComponent(CommonGameComponentTypes.DISABLE_VANILLA_MUSIC)) {
            ci.cancel();

            // Disable any currently playing music if the setting just got enabled!
            if (currentMusic != null) {
                stopPlaying();
            }
        }
    }
}
