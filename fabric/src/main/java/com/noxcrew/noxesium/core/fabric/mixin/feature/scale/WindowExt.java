package com.noxcrew.noxesium.core.fabric.mixin.feature.scale;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.platform.Window;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.fabric.feature.ScalingExtension;
import com.noxcrew.noxesium.core.feature.GuiElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Window.class)
public abstract class WindowExt implements ScalingExtension {

    @Unique
    private float noxesium$divisor = 1;

    @Override
    public void noxesium$whileRescaled(GuiElement element, Runnable function) {
        var config = NoxesiumMod.getInstance().getConfig();
        var old = noxesium$divisor;
        noxesium$divisor = (float) config.getScale(element);
        function.run();
        noxesium$divisor = old;
    }

    @ModifyReturnValue(method = "getGuiScaledWidth", at = @At("TAIL"))
    public int wrapGetWidth(int original) {
        return (int) (original / noxesium$divisor);
    }

    @ModifyReturnValue(method = "getGuiScaledHeight", at = @At("TAIL"))
    public int wrapGetHeight(int original) {
        return (int) (original / noxesium$divisor);
    }
}
