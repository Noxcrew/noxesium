package com.noxcrew.noxesium.mixin.render;

import com.noxcrew.noxesium.feature.render.CustomShaderManager;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Registers custom shaders to the game renderer.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "reloadShaders", at = @At("TAIL"))
    private void reloadShaders(ResourceProvider provider, CallbackInfo ci) {
        CustomShaderManager.reloadShaders(((GameRenderer) ((Object) this)), provider);
    }
}
