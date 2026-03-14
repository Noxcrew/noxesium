package com.noxcrew.noxesium.core.fabric.mixin.feature.scale;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.fabric.feature.ScalingExtension;
import com.noxcrew.noxesium.core.feature.GuiElement;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Gui.class)
public class ScoreboardMixin {
    @WrapMethod(method = "displayScoreboardSidebar")
    public void wrapScoreboardRender(GuiGraphicsExtractor guiGraphics, Objective objective, Operation<Void> original) {
        var config = NoxesiumMod.getInstance().getConfig();
        ((ScalingExtension) guiGraphics).noxesium$whileRescaled(GuiElement.SCOREBOARD, () -> {
            // Determine the position based on the height so it always fits on-screen!
            var lineCount =
                    objective.getScoreboard().listPlayerScores(objective).size();
            if (lineCount > 15) lineCount = 15;
            var height = lineCount * 9 + 9;
            var scaledHeight = ((float) height) * config.getScale(GuiElement.SCOREBOARD);
            guiGraphics.pose().translate(0, (float)
                    (-config.scoreboardPosition * ((float) guiGraphics.guiHeight() - scaledHeight) / 2f));
            original.call(guiGraphics, objective);
        });
    }

    @ModifyConstant(method = "displayScoreboardSidebar", constant = @Constant(intValue = 3, ordinal = 0))
    public int wrapGetWidth(int constant) {
        // Vanilla divides the lines * 9 by 3 which means the scoreboard is not properly centered.
        return 2;
    }

    @WrapOperation(
            method = "displayScoreboardSidebar",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;guiHeight()I"))
    public int wrapGetHeight(GuiGraphicsExtractor instance, Operation<Integer> original) {
        // Increase the height by 9 to account for the header which vanilla does not otherwise account for.
        return original.call(instance) + 9;
    }
}
