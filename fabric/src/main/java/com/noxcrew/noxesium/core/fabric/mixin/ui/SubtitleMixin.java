package com.noxcrew.noxesium.core.fabric.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.api.client.GuiElement;
import com.noxcrew.noxesium.core.fabric.feature.render.GuiGraphicsScalingExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SubtitleOverlay;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SubtitleOverlay.class)
public class SubtitleMixin {
    @WrapMethod(method = "render")
    public void wrapSubtitleRender(GuiGraphics guiGraphics, Operation<Void> original) {
        ((GuiGraphicsScalingExtension) guiGraphics).noxesium$whileRescaled(GuiElement.SUBTITLES, () -> {
            original.call(guiGraphics);
        });
    }
}
