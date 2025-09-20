package com.noxcrew.noxesium.core.fabric.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.api.client.GuiElement;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SubtitleOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SubtitleOverlay.class)
public class SubtitleMixin {
    @WrapMethod(method = "render")
    public void wrapSubtitleRender(GuiGraphics guiGraphics, Operation<Void> original) {
        guiGraphics.pose().pushMatrix();
        var config = NoxesiumMod.getInstance().getConfig();
        guiGraphics.pose().scale((float) config.getScale(GuiElement.SUBTITLES));
        original.call(guiGraphics);
        guiGraphics.pose().popMatrix();
    }

    @WrapOperation(
        method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;guiWidth()I"))
    public int wrapGetWidth(GuiGraphics instance, Operation<Integer> original) {
        return (int) (original.call(instance) / NoxesiumMod.getInstance().getConfig().getScale(GuiElement.SUBTITLES));
    }

    @WrapOperation(
        method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;guiHeight()I"))
    public int wrapGetHeight(GuiGraphics instance, Operation<Integer> original) {
        return (int) (original.call(instance) / NoxesiumMod.getInstance().getConfig().getScale(GuiElement.SUBTITLES));
    }
}
