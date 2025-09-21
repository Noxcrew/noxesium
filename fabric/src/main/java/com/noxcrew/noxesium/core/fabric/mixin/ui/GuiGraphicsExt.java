package com.noxcrew.noxesium.core.fabric.mixin.ui;

import com.noxcrew.noxesium.api.client.GuiElement;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.fabric.feature.render.GuiGraphicsScalingExtension;
import com.noxcrew.noxesium.core.fabric.feature.render.WindowScalingExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsExt implements GuiGraphicsScalingExtension {
    @Shadow
    @Final
    private Matrix3x2fStack pose;

    @Override
    public void noxesium$whileRescaled(GuiElement element, Runnable function) {
        var config = NoxesiumMod.getInstance().getConfig();
        var scale = (float) config.getScale(element);
        pose.pushMatrix();
        pose.scale(scale);
        ((WindowScalingExtension) Minecraft.getInstance().getWindow()).noxesium$whileRescaled(element, function);
        pose.popMatrix();
    }
}
