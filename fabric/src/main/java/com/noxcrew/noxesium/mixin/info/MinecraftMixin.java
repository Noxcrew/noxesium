package com.noxcrew.noxesium.mixin.info;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.render.cache.ElementBuffer;
import com.noxcrew.noxesium.feature.render.cache.ElementCache;
import com.noxcrew.noxesium.feature.render.cache.chat.ChatCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Syncs up with the server whenever the GUI scale is updated.
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow @Nullable public Screen screen;

    @Inject(method = "resizeDisplay", at = @At(value = "TAIL"))
    private void resizeDisplay(CallbackInfo ci) {
        for (var cache : ElementCache.getAllCaches()) {
            cache.clearCache();
        }
    }

    @Inject(method = "setScreen", at = @At(value = "HEAD"))
    private void setScreen(Screen newScreen, CallbackInfo ci) {
        if (newScreen instanceof ChatScreen || this.screen instanceof ChatScreen) {
            ChatCache.getInstance().clearCache();
        }
    }

    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen(II)V"))
    private void injected(RenderTarget instance, int width, int height) {
        if (NoxesiumMod.shouldDisableExperimentalPerformancePatches() || ElementBuffer.CURRENT_BUFFER == null) {
            instance.blitToScreen(width, height);
        } else {
            ElementBuffer.CURRENT_BUFFER.blitToScreen(width, height);
        }
    }
}
