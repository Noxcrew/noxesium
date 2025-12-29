package com.noxcrew.noxesium.core.fabric.mixin.feature.scale;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.core.fabric.feature.ScalingExtension;
import com.noxcrew.noxesium.core.feature.GuiElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Gui.class)
public class RescaleGuiMixin {
    @WrapMethod(method = "renderOverlayMessage")
    public void wrapActionBarRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        ((ScalingExtension) guiGraphics).noxesium$whileRescaled(GuiElement.ACTION_BAR, () -> {
            original.call(guiGraphics, deltaTracker);
        });
    }

    @WrapMethod(method = "renderEffects")
    public void wrapEffectsRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        ((ScalingExtension) guiGraphics).noxesium$whileRescaled(GuiElement.ACTION_BAR, () -> {
            original.call(guiGraphics, deltaTracker);
        });
    }

    @WrapMethod(method = "renderTitle")
    public void wrapTitleRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        ((ScalingExtension) guiGraphics).noxesium$whileRescaled(GuiElement.TITLE, () -> {
            original.call(guiGraphics, deltaTracker);
        });
    }

    @WrapMethod(method = "renderHotbarAndDecorations")
    public void wrapHotbarRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        ((ScalingExtension) guiGraphics).noxesium$whileRescaled(GuiElement.HOTBAR, () -> {
            original.call(guiGraphics, deltaTracker);
        });
    }
}
