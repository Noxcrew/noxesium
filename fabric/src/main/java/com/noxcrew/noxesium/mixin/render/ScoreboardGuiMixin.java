package com.noxcrew.noxesium.mixin.render;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.render.ScoreboardRenderer;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(Gui.class)
public abstract class ScoreboardGuiMixin {

    @Shadow private int screenHeight;

    @Shadow private int screenWidth;

    @Shadow @Final private Minecraft minecraft;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/scores/Objective;)V"))
    private void injected(Gui instance, GuiGraphics guiGraphics, Objective objective) {
        if (NoxesiumMod.DEBUG_DISABLE_PATCHES) {
            instance.displayScoreboardSidebar(guiGraphics, objective);
        } else {
            ScoreboardRenderer.renderScoreboard(guiGraphics, screenWidth, screenHeight, minecraft);
        }
    }
}
