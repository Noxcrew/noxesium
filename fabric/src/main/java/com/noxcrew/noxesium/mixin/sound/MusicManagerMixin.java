package com.noxcrew.noxesium.mixin.sound;

import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into vanilla's music manager to fully disable vanilla music while its active.
 */
@Mixin(MusicManager.class)
public abstract class MusicManagerMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo ci) {
        // Prevent vanilla music from ticking and starting to play whenever custom music is running
        if (ServerRules.ENABLE_CUSTOM_MUSIC.getValue()) ci.cancel();
    }
}
