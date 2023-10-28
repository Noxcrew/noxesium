package com.noxcrew.noxesium.mixin.info;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.render.cache.ElementBuffer;
import com.noxcrew.noxesium.feature.render.cache.actionbar.ActionBarCache;
import com.noxcrew.noxesium.feature.render.cache.bossbar.BossBarCache;
import com.noxcrew.noxesium.feature.render.cache.scoreboard.ScoreboardCache;
import com.noxcrew.noxesium.feature.render.cache.tablist.TabListCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Syncs up with the server whenever the GUI scale is updated.
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "resizeDisplay", at = @At(value = "TAIL"))
    private void resizeDisplay(CallbackInfo ci) {
        ScoreboardCache.getInstance().clearCache();
        BossBarCache.getInstance().clearCache();
        ActionBarCache.getInstance().clearCache();
        TabListCache.getInstance().clearCache();
    }

    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen(II)V"))
    private void injected(RenderTarget instance, int width, int height) {
        if (NoxesiumMod.DEBUG_DISABLE_PATCHES || ElementBuffer.CURRENT_BUFFER == null) {
            instance.blitToScreen(width, height);
        } else {
            ElementBuffer.CURRENT_BUFFER.blitToScreen(width, height);
        }
    }
}
