package com.noxcrew.noxesium.mixin.performance.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SubtitleOverlay.class)
public abstract class SubTitlesOverlayMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(GuiGraphics graphics, CallbackInfo ci) {
        // TODO implement optimizations
    }
}
