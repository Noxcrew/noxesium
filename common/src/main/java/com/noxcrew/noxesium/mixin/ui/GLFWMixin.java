package com.noxcrew.noxesium.mixin.ui;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.ui.render.api.NoxesiumRenderStateHolder;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into frame buffer swapping.
 */
@Mixin(value = GLFW.class, remap = false)
public class GLFWMixin {

    @Inject(method = "glfwSwapBuffers", at = @At("RETURN"))
    private static void glfwSwapBuffers(long window, CallbackInfo ci) {
        NoxesiumMod.forEachRenderStateHolder(NoxesiumRenderStateHolder::trySnapshot);
    }
}
