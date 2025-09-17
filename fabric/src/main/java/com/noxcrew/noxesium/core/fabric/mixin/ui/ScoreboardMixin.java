package com.noxcrew.noxesium.core.fabric.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Gui.class)
public class ScoreboardMixin {
    @WrapMethod(method = "displayScoreboardSidebar")
    public void wrapScoreboardRender(GuiGraphics guiGraphics, Objective objective, Operation<Void> original) {
        guiGraphics.pose().pushMatrix();
        var config = NoxesiumMod.getInstance().getConfig();
        guiGraphics.pose().scale((float) config.scoreboardScale);
        guiGraphics.pose().translate(0, (float)
                (-config.scoreboardPosition * ((double) guiGraphics.guiHeight()) / config.scoreboardScale / 2.0));
        original.call(guiGraphics, objective);
        guiGraphics.pose().popMatrix();
    }

    @ModifyConstant(method = "displayScoreboardSidebar", constant = @Constant(intValue = 3, ordinal = 0))
    public int wrapGetWidth(int constant) {
        // Vanilla divides the lines * 9 by 3 which means the scoreboard is not properly centered.
        return 2;
    }

    @WrapOperation(
            method = "displayScoreboardSidebar",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;guiWidth()I"))
    public int wrapGetWidth(GuiGraphics instance, Operation<Integer> original) {
        return (int) (original.call(instance) / NoxesiumMod.getInstance().getConfig().scoreboardScale);
    }

    @WrapOperation(
            method = "displayScoreboardSidebar",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;guiHeight()I"))
    public int wrapGetHeight(GuiGraphics instance, Operation<Integer> original) {
        // Increase the height by 9 to account for the header which vanilla does not otherwise account for.
        return (int) (original.call(instance) / NoxesiumMod.getInstance().getConfig().scoreboardScale) + 9;
    }
}
