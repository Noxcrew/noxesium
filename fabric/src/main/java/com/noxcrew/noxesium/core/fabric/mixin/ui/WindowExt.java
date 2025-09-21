package com.noxcrew.noxesium.core.fabric.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.Window;
import com.noxcrew.noxesium.api.client.GuiElement;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.fabric.feature.render.WindowScalingExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Window.class)
public abstract class WindowExt implements WindowScalingExtension {
    @Shadow
    public abstract int getGuiScaledWidth();

    @Shadow
    public abstract int getGuiScaledHeight();

    @Unique
    private Integer noxesium$guiWidth = null;

    @Unique
    private Integer noxesium$guiHeight = null;

    @Override
    public void noxesium$whileRescaled(GuiElement element, Runnable function) {
        var config = NoxesiumMod.getInstance().getConfig();
        var scale = (float) config.getScale(element);
        var oldWidth = noxesium$guiWidth;
        var oldHeight = noxesium$guiHeight;
        noxesium$guiWidth = (int) (getGuiScaledWidth() / scale);
        noxesium$guiHeight = (int) (getGuiScaledHeight() / scale);
        function.run();
        noxesium$guiWidth = oldWidth;
        noxesium$guiHeight = oldHeight;
    }

    @WrapMethod(method = "getGuiScaledWidth")
    public int wrapGetWidth(Operation<Integer> original) {
        return noxesium$guiWidth == null ? original.call() : noxesium$guiWidth;
    }

    @WrapMethod(method = "getGuiScaledHeight")
    public int wrapGetHeight(Operation<Integer> original) {
        return noxesium$guiHeight == null ? original.call() : noxesium$guiHeight;
    }
}
