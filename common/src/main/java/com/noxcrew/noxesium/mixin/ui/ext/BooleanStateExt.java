package com.noxcrew.noxesium.mixin.ui.ext;

import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Grants access to the enabled state of a boolean state.
 */
@Mixin(GlStateManager.BooleanState.class)
public interface BooleanStateExt {

    @Accessor("enabled")
    boolean isEnabled();
}
