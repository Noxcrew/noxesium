package com.noxcrew.noxesium.core.fabric.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.api.client.GuiElement;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
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
        guiGraphics.pose().pushMatrix();
        var config = NoxesiumMod.getInstance().getConfig();
        guiGraphics.pose().scale((float) config.getScale(GuiElement.TAB_LIST));
        original.call(guiGraphics, (int) (guiWidth / config.getScale(GuiElement.TAB_LIST)), scoreboard, objective);
        guiGraphics.pose().popMatrix();
    }
}
