package com.noxcrew.noxesium.mixin.entity;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DisplayRenderer.TextDisplayRenderer.class)
public class TextDisplayRenderer {

    /**
     * Normal text backgrounds like on name tags render the background as see-through and the text as whatever the selected mode is. However,
     * text displays render the background as non-see-through for some reason? We fix this by just always rendering the backgrounds as see-through
     * which fixes issues with its transparency. This mirrors entity name tags.
     */
    @Redirect(method = "renderInner(Lnet/minecraft/world/entity/Display$TextDisplay;Lnet/minecraft/world/entity/Display$TextDisplay$TextRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;textBackground()Lnet/minecraft/client/renderer/RenderType;"))
    public RenderType injected() {
        return RenderType.textBackgroundSeeThrough();
    }
}
