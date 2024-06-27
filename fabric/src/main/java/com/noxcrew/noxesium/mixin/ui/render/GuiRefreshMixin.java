package com.noxcrew.noxesium.mixin.ui.render;

import com.noxcrew.noxesium.feature.ui.wrapper.ElementWrapper;
import com.noxcrew.noxesium.feature.ui.wrapper.ElementManager;
import com.noxcrew.noxesium.feature.ui.wrapper.ActionBarWrapper;
import com.noxcrew.noxesium.feature.ui.wrapper.TitleWrapper;
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
public abstract class GuiRefreshMixin {

    @Inject(method = "setOverlayMessage", at = @At(value = "TAIL"))
    private void refreshActionBarOnSetOverlayMessage(Component component, boolean bl, CallbackInfo ci) {
        ElementManager.getInstance(ActionBarWrapper.class).requestRedraw();
    }

    @Inject(method = "resetTitleTimes", at = @At(value = "TAIL"))
    private void refreshTitleCacheOnResetTitleTimes(CallbackInfo ci) {
        ElementManager.getInstance(TitleWrapper.class).requestRedraw();
    }

    @Inject(method = "setTimes", at = @At(value = "TAIL"))
    private void refreshTitleCacheOnSetTimes(CallbackInfo ci) {
        ElementManager.getInstance(TitleWrapper.class).requestRedraw();
    }

    @Inject(method = "setSubtitle", at = @At(value = "TAIL"))
    private void refreshTitleCacheOnSetSubtitle(CallbackInfo ci) {
        ElementManager.getInstance(TitleWrapper.class).requestRedraw();
    }

    @Inject(method = "setTitle", at = @At(value = "TAIL"))
    private void refreshTitleCacheOnSetTitle(CallbackInfo ci) {
        ElementManager.getInstance(TitleWrapper.class).requestRedraw();
    }

    @Inject(method = "clear", at = @At(value = "TAIL"))
    private void refreshTitleCacheOnClear(CallbackInfo ci) {
        ElementManager.getInstance(TitleWrapper.class).requestRedraw();
    }

    @Inject(method = "onDisconnected", at = @At(value = "TAIL"))
    private void refreshElementsOnDisconnect(CallbackInfo ci) {
        ElementManager.getAllWrappers().forEach(ElementWrapper::requestRedraw);
    }
}
