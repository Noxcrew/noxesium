package com.noxcrew.noxesium.core.fabric.mixin.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noxcrew.noxesium.core.fabric.feature.render.CustomRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DisplayRenderer.TextDisplayRenderer.class)
public abstract class FixTextDisplayTransparencyMixin {

    /**
     * Normal text backgrounds like on name tags render the background as see-through and the text as whatever the selected mode is. However,
     * text displays render the background as non-see-through. We fix this by just always rendering the backgrounds as see-through
     * which fixes issues with its transparency. We do however use a custom type with a depth buffer that ensures the background does not
     * render through walls. We want to render things behind it, not it behind other things.
     */
    @WrapOperation(
            method =
                    "renderInner(Lnet/minecraft/client/renderer/entity/state/TextDisplayEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IF)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/RenderType;textBackground()Lnet/minecraft/client/renderer/RenderType;"))
    public RenderType determineRenderType(Operation<RenderType> original) {
        return CustomRenderTypes.textBackgroundSeeThroughWithDepth();
    }
}
