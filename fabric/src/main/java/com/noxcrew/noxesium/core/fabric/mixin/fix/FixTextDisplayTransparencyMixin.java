package com.noxcrew.noxesium.core.fabric.mixin.fix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderPipelines.class)
public abstract class FixTextDisplayTransparencyMixin {
    @WrapOperation(
            method = "<clinit>",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/blaze3d/pipeline/RenderPipeline$Builder;build()Lcom/mojang/blaze3d/pipeline/RenderPipeline;"))
    private static RenderPipeline onBuildPipeline(RenderPipeline.Builder instance, Operation<RenderPipeline> original) {
        if (instance.location.isPresent()) {
            var path = instance.location.get().getPath();
            if (path.equals("pipeline/text_background")) {
                /*
                 * MC-259812:
                 * Normal text backgrounds like on name tags render the background as see-through and the text as whatever the selected mode is. However,
                 * text displays render the background as non-see-through. We fix this by just always rendering the backgrounds as see-through
                 * which fixes issues with its transparency. We modify the vanilla type so Iris doesn't break.
                 */
                instance.withDepthWrite(false);
                instance.withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST);
            }
        }
        return original.call(instance);
    }
}
