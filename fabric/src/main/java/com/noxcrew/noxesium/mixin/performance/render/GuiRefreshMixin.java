package com.noxcrew.noxesium.mixin.performance.render;

import com.noxcrew.noxesium.feature.render.cache.ElementCache;
import com.noxcrew.noxesium.feature.render.cache.actionbar.ActionBarCache;
import com.noxcrew.noxesium.feature.render.cache.title.TitleCache;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixes in to Gui to trigger refreshes of various cached elements.
 */
@Mixin(Gui.class)
public class GuiRefreshMixin {

    @Inject(method = "setOverlayMessage", at = @At(value = "TAIL"))
    private void setOverlayMessage(Component component, boolean bl, CallbackInfo ci) {
        ActionBarCache.getInstance().clearCache();
    }

    @Inject(method = "resetTitleTimes", at = @At(value = "TAIL"))
    private void resetTitleTimes(CallbackInfo ci) {
        TitleCache.getInstance().clearCache();
    }

    @Inject(method = "setTimes", at = @At(value = "TAIL"))
    private void setTimes(CallbackInfo ci) {
        TitleCache.getInstance().clearCache();
    }

    @Inject(method = "setSubtitle", at = @At(value = "TAIL"))
    private void setSubtitle(CallbackInfo ci) {
        TitleCache.getInstance().clearCache();
    }

    @Inject(method = "setTitle", at = @At(value = "TAIL"))
    private void setTitle(CallbackInfo ci) {
        TitleCache.getInstance().clearCache();
    }

    @Inject(method = "clear", at = @At(value = "TAIL"))
    private void clear(CallbackInfo ci) {
        TitleCache.getInstance().clearCache();
    }

    @Inject(method = "onDisconnected", at = @At(value = "TAIL"))
    private void onDisconnect(CallbackInfo ci) {
        ElementCache.getAllCaches().forEach(ElementCache::clearCache);
    }
}
