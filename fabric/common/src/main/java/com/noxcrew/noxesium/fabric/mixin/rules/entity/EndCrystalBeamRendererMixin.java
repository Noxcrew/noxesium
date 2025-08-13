package com.noxcrew.noxesium.fabric.mixin.rules.entity;

import static com.noxcrew.noxesium.fabric.feature.entity.EndCrystalRenderHolder.noxesium$endCrystalBeamColor;
import static com.noxcrew.noxesium.fabric.feature.entity.EndCrystalRenderHolder.noxesium$endCrystalBeamColorFade;
import static com.noxcrew.noxesium.fabric.feature.entity.EndCrystalRenderHolder.noxesium$index;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.resources.ResourceLocation;
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
                                    "Lnet/minecraft/client/renderer/RenderType;entitySmoothCutout(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    private static RenderType determineRenderType(ResourceLocation resourceLocation) {
        return RenderType.entityTranslucent(resourceLocation, true);
    }

    /**
     * Override the vertex color used when drawing.
     */
    @Redirect(
            method = "renderCrystalBeams",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(I)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private static VertexConsumer overrideColor(VertexConsumer vertexConsumer, int i) {
        if (noxesium$endCrystalBeamColor != null) {
            if (noxesium$endCrystalBeamColorFade != null) {
                var ind = noxesium$index;
                noxesium$index++;
                if (noxesium$index >= 4) {
                    noxesium$index = 0;
                }
                if (ind == 0 || ind == 3) {
                    vertexConsumer.setColor(noxesium$endCrystalBeamColorFade);
                } else {
                    vertexConsumer.setColor(noxesium$endCrystalBeamColor);
                }
            } else {
                vertexConsumer.setColor(noxesium$endCrystalBeamColor);
            }
        } else {
            vertexConsumer.setColor(i);
        }
        return vertexConsumer;
    }
}
