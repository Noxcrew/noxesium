package com.noxcrew.noxesium.core.fabric.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.api.client.GuiElement;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public class TitleMixin {
    @WrapMethod(method = "renderTitle")
    public void wrapTitleRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        guiGraphics.pose().pushMatrix();
        var config = NoxesiumMod.getInstance().getConfig();
        guiGraphics.pose().scale((float) config.getScale(GuiElement.TITLE));
        original.call(guiGraphics, deltaTracker);
        guiGraphics.pose().popMatrix();
    }

    @WrapOperation(
            method = "renderTitle",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;guiWidth()I"))
    public int wrapGetWidth(GuiGraphics instance, Operation<Integer> original) {
        return (int) (original.call(instance) / NoxesiumMod.getInstance().getConfig().getScale(GuiElement.TITLE));
    }

    @WrapOperation(
            method = "renderTitle",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;guiHeight()I"))
    public int wrapGetHeight(GuiGraphics instance, Operation<Integer> original) {
        return (int) (original.call(instance) / NoxesiumMod.getInstance().getConfig().getScale(GuiElement.TITLE));
    }
}
