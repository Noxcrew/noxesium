package com.noxcrew.noxesium.mixin.ui.ext;

import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Allows accessing the blending state.
 */
@Mixin(GlStateManager.class)
public interface GlStateManagerExt {

    @Accessor("BLEND")
    static GlStateManager.BlendState getBlendingState() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
