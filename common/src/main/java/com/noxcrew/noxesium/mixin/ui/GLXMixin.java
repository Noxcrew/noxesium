package com.noxcrew.noxesium.mixin.ui;

import com.mojang.blaze3d.platform.GLX;
import com.noxcrew.noxesium.config.NoxesiumConfig;
import org.lwjgl.opengl.GL;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into the start-up sequence to check if dynamic UI limiting can be used.
 */
@Mixin(value = GLX.class, remap = false)
public class GLXMixin {

    @Inject(method = "_init", at = @At("RETURN"))
    private static void init(int p_69344_, boolean p_69345_, CallbackInfo ci) {
        NoxesiumConfig.supportsDynamicUiLimiting = GL.getCapabilities().GL_ARB_buffer_storage;
    }
}
