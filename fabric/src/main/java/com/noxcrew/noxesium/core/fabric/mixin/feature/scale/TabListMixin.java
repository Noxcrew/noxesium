package com.noxcrew.noxesium.core.fabric.mixin.feature.scale;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.core.feature.GuiElement;
import com.noxcrew.noxesium.core.fabric.feature.ScalingExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerTabOverlay.class)
public class TabListMixin {
    @WrapMethod(method = "render")
    public void wrapTabListRender(
            GuiGraphics guiGraphics,
            int guiWidth,
            Scoreboard scoreboard,
            Objective objective,
            Operation<Void> original) {
        ((ScalingExtension) guiGraphics).noxesium$whileRescaled(GuiElement.TAB_LIST, () -> {
            original.call(guiGraphics, guiGraphics.guiWidth(), scoreboard, objective);
        });
    }
}
