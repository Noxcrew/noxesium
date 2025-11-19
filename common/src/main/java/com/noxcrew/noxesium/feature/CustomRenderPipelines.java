package com.noxcrew.noxesium.feature;

import static net.minecraft.client.renderer.RenderPipelines.register;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;

/**
 * Defines custom render pipelines.
 */
public class CustomRenderPipelines {

    /**
     * Defines a variant of the text_background_see_through render type but
     * with a modified depth state and no depth write.
     */
    public static final RenderPipeline TEXT_BACKGROUND_SEE_THROUGH_WITH_DEPTH =
            register(RenderPipeline.builder(RenderPipelines.TEXT_SNIPPET)
                    .withLocation("pipeline/text_background_see_through")
                    .withVertexShader("core/rendertype_text_background_see_through")
                    .withFragmentShader("core/rendertype_text_background_see_through")
                    .withDepthWrite(false)
                    .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS)
                    .build());
}
