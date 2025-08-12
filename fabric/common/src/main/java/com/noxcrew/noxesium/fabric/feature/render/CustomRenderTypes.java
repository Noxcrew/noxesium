package com.noxcrew.noxesium.fabric.feature.render;

import static net.minecraft.client.renderer.RenderStateShard.DEFAULT_LINE;
import static net.minecraft.client.renderer.RenderStateShard.ITEM_ENTITY_TARGET;
import static net.minecraft.client.renderer.RenderStateShard.LIGHTMAP;
import static net.minecraft.client.renderer.RenderStateShard.NO_TEXTURE;
import static net.minecraft.client.renderer.RenderStateShard.VIEW_OFFSET_Z_LAYERING;
import static net.minecraft.client.renderer.RenderType.create;

import net.minecraft.client.renderer.RenderType;

/**
 * Defines custom render types.
 */
public class CustomRenderTypes {

    /**
     * Defines a variant of the text_background_see_through render type but
     * with a modified depth state.
     */
    private static final RenderType TEXT_BACKGROUND_SEE_THROUGH_WITH_DEPTH = create(
            "text_background_see_through_with_depth",
            1536,
            false,
            true,
            CustomRenderPipelines.TEXT_BACKGROUND_SEE_THROUGH_WITH_DEPTH,
            RenderType.CompositeState.builder()
                    .setTextureState(NO_TEXTURE)
                    .setLightmapState(LIGHTMAP)
                    .createCompositeState(false));

    /**
     * Can be used to draw lines that have no depth sorting.
     */
    private static final RenderType LINES_NO_DEPTH = create(
            "nodepth-lines",
            1536,
            false,
            false,
            CustomRenderPipelines.LINES_NO_DEPTH,
            RenderType.CompositeState.builder()
                    .setLineState(DEFAULT_LINE)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .createCompositeState(false));

    public static RenderType textBackgroundSeeThroughWithDepth() {
        return TEXT_BACKGROUND_SEE_THROUGH_WITH_DEPTH;
    }

    public static RenderType linesNoDepth() {
        return LINES_NO_DEPTH;
    }
}
