package com.noxcrew.noxesium.core.fabric.mixin.feature.scale;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.fabric.feature.ScalingExtension;
import com.noxcrew.noxesium.core.feature.GuiElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BossHealthOverlay.class)
public class BossBarMixin {
    @WrapMethod(method = "render")
    public void wrapBossBarRender(GuiGraphics guiGraphics, Operation<Void> original) {
        var config = NoxesiumMod.getInstance().getConfig();
        ((ScalingExtension) guiGraphics).noxesium$whileRescaled(GuiElement.BOSS_BAR, () -> {
            guiGraphics.pose().translate((float) (config.bossBarPosition * ((double) guiGraphics.guiWidth()) / 2.0), 0);
            original.call(guiGraphics);
        });
    }
}
