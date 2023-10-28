package com.noxcrew.noxesium.mixin.render;

import com.noxcrew.noxesium.feature.render.cache.tablist.TabListCache;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @Inject(method = "setHeader", at = @At(value = "TAIL"))
    private void setHeader(CallbackInfo ci) {
        TabListCache.getInstance().clearCache();
    }

    @Inject(method = "setFooter", at = @At(value = "TAIL"))
    private void setFooter(CallbackInfo ci) {
        TabListCache.getInstance().clearCache();
    }

    @Inject(method = "reset", at = @At(value = "TAIL"))
    private void reset(CallbackInfo ci) {
        TabListCache.getInstance().clearCache();
    }

    @Inject(method = "setVisible", at = @At(value = "INVOKE", target = "Ljava/util/Map;clear()V"))
    private void setVisible(CallbackInfo ci) {
        TabListCache.getInstance().resetHearts();
    }
}
