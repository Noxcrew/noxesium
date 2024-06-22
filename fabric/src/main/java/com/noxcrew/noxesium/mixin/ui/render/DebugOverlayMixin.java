package com.noxcrew.noxesium.mixin.ui.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.feature.ui.cache.DebugWrapper;
import com.noxcrew.noxesium.feature.ui.cache.ElementManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DebugScreenOverlay.class)
public abstract class DebugOverlayMixin {

    @WrapOperation(method = "render", at = @At("HEAD"))
    public void render(GuiGraphics graphics, Operation<Void> original) {
        ElementManager.getInstance(DebugWrapper.class).wrapOperation(graphics, original);
    }
}
