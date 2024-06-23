package com.noxcrew.noxesium.mixin.ui.render;

import com.noxcrew.noxesium.feature.ui.wrapper.ElementManager;
import com.noxcrew.noxesium.feature.ui.wrapper.TabListWrapper;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {

    @Inject(method = "setHeader", at = @At("TAIL"))
    private void refreshTabListOnSetHeader(CallbackInfo ci) {
        ElementManager.getInstance(TabListWrapper.class).requestRedraw();
    }

    @Inject(method = "setFooter", at = @At("TAIL"))
    private void refreshTabListOnSetFooter(CallbackInfo ci) {
        ElementManager.getInstance(TabListWrapper.class).requestRedraw();
    }

    @Inject(method = "reset", at = @At("TAIL"))
    private void refreshTabListOnReset(CallbackInfo ci) {
        ElementManager.getInstance(TabListWrapper.class).requestRedraw();
    }

    @Inject(method = "setVisible", at = @At(value = "INVOKE", target = "Ljava/util/Map;clear()V"))
    private void refreshTabListOnSetVisible(CallbackInfo ci) {
        ElementManager.getInstance(TabListWrapper.class).requestRedraw();
    }
}
