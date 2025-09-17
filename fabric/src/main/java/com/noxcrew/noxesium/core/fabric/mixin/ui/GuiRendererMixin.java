package com.noxcrew.noxesium.core.fabric.mixin.ui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Normally if we render UIs at smaller scales it will end up looking terrible
 * because there is not enough resolution. So we render everything at the smallest
 * UI scale we need to have a good resolution based on the settings.
 */
@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
    @WrapOperation(
            method = "draw",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/CachedOrthoProjectionMatrixBuffer;getBuffer(FF)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"))
    public GpuBufferSlice wrapBufferResize(
            CachedOrthoProjectionMatrixBuffer instance, float width, float height, Operation<GpuBufferSlice> original) {
        return original.call(instance, width * 2, height * 2);
    }
}
