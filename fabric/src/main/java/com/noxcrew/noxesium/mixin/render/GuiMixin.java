package com.noxcrew.noxesium.mixin.render;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.render.cache.bossbar.BossBarCache;
import com.noxcrew.noxesium.feature.render.cache.ScoreboardCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    private int screenHeight;

    @Shadow
    private int screenWidth;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/scores/Objective;)V"))
    private void injected(Gui instance, GuiGraphics guiGraphics, Objective objective) {
        if (NoxesiumMod.DEBUG_DISABLE_PATCHES) {
            instance.displayScoreboardSidebar(guiGraphics, objective);
        } else {
            ScoreboardCache.getInstance().renderDirect(guiGraphics, ScoreboardCache.getInstance().getCache(), screenWidth, screenHeight, minecraft);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;render(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    private void injected(BossHealthOverlay instance, GuiGraphics guiGraphics) {
        if (NoxesiumMod.DEBUG_DISABLE_PATCHES) {
            instance.render(guiGraphics);
        } else {
            BossBarCache.getInstance().renderDirect(guiGraphics, BossBarCache.getInstance().getCache(), screenWidth, screenHeight, minecraft);
        }
    }
}
