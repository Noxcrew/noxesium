package com.noxcrew.noxesium.core.fabric.mixin.feature.entity.beam;

import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Hooks into end crystal beam rendering.
 */
@Mixin(EnderDragonRenderer.class)
public class EndCrystalBeamRendererMixin {

    /**
     * Swap out the type of rendering for a transparent one so we can properly make it transparent.
     */
    @Redirect(
            method = "<clinit>",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/rendertype/RenderTypes;entitySmoothCutout(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private static RenderType determineRenderType(Identifier Identifier) {
        return RenderTypes.entityTranslucent(Identifier);
    }
}
