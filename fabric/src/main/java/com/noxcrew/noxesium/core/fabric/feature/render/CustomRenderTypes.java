package com.noxcrew.noxesium.core.fabric.feature.render;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * Defines custom render types.
 */
public class CustomRenderTypes {

    /**
     * Defines a variant of the text_background_see_through render type but
     * with a modified depth state.
     */
    private static final RenderType TEXT_BACKGROUND_SEE_THROUGH_WITH_DEPTH = RenderType.create(
            "text_background_see_through_with_depth",
            RenderSetup.builder(CustomRenderPipelines.TEXT_BACKGROUND_SEE_THROUGH_WITH_DEPTH)
                    .useLightmap()
                    .sortOnUpload()
                    .createRenderSetup());

    public static RenderType textBackgroundSeeThroughWithDepth() {
        return TEXT_BACKGROUND_SEE_THROUGH_WITH_DEPTH;
    }
}
