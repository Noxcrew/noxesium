package com.noxcrew.noxesium.mixin.ui.render;

import com.noxcrew.noxesium.feature.ui.wrapper.ElementManager;
import com.noxcrew.noxesium.feature.ui.wrapper.TabListWrapper;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInfo.class)
public abstract class PlayerInfoMixin {

    @Shadow
    private int latency;

    @Inject(method = "setLatency", at = @At("HEAD"))
    private void refreshTabListOnLatencyChange(int newLatency, CallbackInfo ci) {
        int currentLatency = this.latency;

        // Only clear the cache if the latency bucket changes! So we don't update
        // for small edits only ones that edit the visuals.
        var cache = ElementManager.getInstance(TabListWrapper.class);
        if (cache.getLatencyBucket(currentLatency) != cache.getLatencyBucket(newLatency)) {
            cache.requestRedraw();
        }
    }
}
