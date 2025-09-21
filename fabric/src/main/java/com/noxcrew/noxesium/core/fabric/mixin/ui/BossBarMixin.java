package com.noxcrew.noxesium.core.fabric.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.api.client.GuiElement;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.fabric.feature.render.GuiGraphicsScalingExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BossHealthOverlay.class)
public class BossBarMixin {
    @WrapMethod(method = "render")
    public void wrapBossBarRender(GuiGraphics guiGraphics, Operation<Void> original) {
        var config = NoxesiumMod.getInstance().getConfig();
        ((GuiGraphicsScalingExtension) guiGraphics).noxesium$whileRescaled(GuiElement.BOSS_BAR, () -> {
            guiGraphics.pose().translate((float) (config.bossBarPosition * ((double) guiGraphics.guiWidth()) / 2.0), 0);
            original.call(guiGraphics);
        });
    }
}
