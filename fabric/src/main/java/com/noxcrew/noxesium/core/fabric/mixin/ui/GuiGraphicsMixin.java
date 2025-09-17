package com.noxcrew.noxesium.core.fabric.mixin.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {
    @Inject(
            method =
                    "<init>(Lnet/minecraft/client/Minecraft;Lorg/joml/Matrix3x2fStack;Lnet/minecraft/client/gui/render/state/GuiRenderState;)V",
            at = @At("RETURN"))
    public void constructor(Minecraft minecraft, Matrix3x2fStack pose, GuiRenderState guiRenderState, CallbackInfo ci) {
        // Double the scale of all drawings to match the doubled resolution.
        pose.scale(2f);
    }
}
