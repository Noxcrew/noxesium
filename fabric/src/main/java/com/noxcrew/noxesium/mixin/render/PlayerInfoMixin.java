package com.noxcrew.noxesium.mixin.render;

import com.noxcrew.noxesium.feature.render.cache.tablist.TabListCache;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {

    @Shadow
    private int latency;

    @Inject(method = "setLatency", at = @At(value = "HEAD"))
    private void setLatency(int newLatency, CallbackInfo ci) {
        int currentLatency = this.latency;

        // Only clear the cache if the latency bucket changes! So we don't update
        // for small edits only ones that edit the visuals.
        if (TabListCache.getInstance().getLatencyBucket(currentLatency) !=
                TabListCache.getInstance().getLatencyBucket(newLatency)) {
            TabListCache.getInstance().clearCache();
        }
    }
}
