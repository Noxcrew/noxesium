package com.noxcrew.noxesium.mixin.ui.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.feature.ui.cache.ElementManager;
import com.noxcrew.noxesium.feature.ui.cache.TabListWrapper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {

    @WrapOperation(method = "render", at = @At("HEAD"))
    public void renderTabList(GuiGraphics graphics, int partialTicks, Scoreboard scoreboard, Objective objective, Operation<Void> operation) {
        ElementManager.getInstance(TabListWrapper.class).wrapOperation(graphics, partialTicks, scoreboard, objective, operation);
    }

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
