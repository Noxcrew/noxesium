package com.noxcrew.noxesium.mixin.ui;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.ui.render.api.NoxesiumRenderStateHolder;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Trigger voiding of all cached layers when the screen size changes.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(method = "resizeDisplay", at = @At("TAIL"))
    private void refreshElements(CallbackInfo ci) {
        NoxesiumMod.forEachRenderStateHolder(NoxesiumRenderStateHolder::clear);
    }
}
