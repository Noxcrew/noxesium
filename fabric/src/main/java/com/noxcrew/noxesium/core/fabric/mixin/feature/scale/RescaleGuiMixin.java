package com.noxcrew.noxesium.core.fabric.mixin.feature.scale;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.core.fabric.feature.ScalingExtension;
import com.noxcrew.noxesium.core.feature.GuiElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Gui.class)
public class RescaleGuiMixin {
    @WrapMethod(method = "extractOverlayMessage")
    public void wrapActionBarRender(
            GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        ((ScalingExtension) guiGraphics).noxesium$whileRescaled(GuiElement.ACTION_BAR, () -> {
            original.call(guiGraphics, deltaTracker);
        });
    }

    @WrapMethod(method = "extractEffects")
    public void wrapEffectsRender(
            GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        ((ScalingExtension) guiGraphics).noxesium$whileRescaled(GuiElement.ACTION_BAR, () -> {
            original.call(guiGraphics, deltaTracker);
        });
    }

    @WrapMethod(method = "extractTitle")
    public void wrapTitleRender(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        ((ScalingExtension) guiGraphics).noxesium$whileRescaled(GuiElement.TITLE, () -> {
            original.call(guiGraphics, deltaTracker);
        });
    }

    @WrapMethod(method = "extractHotbarAndDecorations")
    public void wrapHotbarRender(
            GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        ((ScalingExtension) guiGraphics).noxesium$whileRescaled(GuiElement.HOTBAR, () -> {
            original.call(guiGraphics, deltaTracker);
        });
    }
}
