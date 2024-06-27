package com.noxcrew.noxesium.mixin.ui.render;

import com.noxcrew.noxesium.feature.ui.wrapper.BossBarWrapper;
import com.noxcrew.noxesium.feature.ui.wrapper.ElementManager;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {

    @Inject(method = "update", at = @At("TAIL"))
    private void refreshBossBarOnUpdate(CallbackInfo ci) {
        ElementManager.getInstance(BossBarWrapper.class).requestRedraw();
    }

    @Inject(method = "reset", at = @At("TAIL"))
    private void refreshBossBarOnReset(CallbackInfo ci) {
        ElementManager.getInstance(BossBarWrapper.class).requestRedraw();
    }
}
