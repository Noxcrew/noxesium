package com.noxcrew.noxesium.feature;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

import static net.minecraft.client.renderer.RenderStateShard.COLOR_DEPTH_WRITE;
import static net.minecraft.client.renderer.RenderStateShard.COLOR_WRITE;
import static net.minecraft.client.renderer.RenderStateShard.DEFAULT_LINE;
import static net.minecraft.client.renderer.RenderStateShard.ITEM_ENTITY_TARGET;
import static net.minecraft.client.renderer.RenderStateShard.LEQUAL_DEPTH_TEST;
import static net.minecraft.client.renderer.RenderStateShard.LIGHTMAP;
import static net.minecraft.client.renderer.RenderStateShard.NO_CULL;
import static net.minecraft.client.renderer.RenderStateShard.NO_DEPTH_TEST;
import static net.minecraft.client.renderer.RenderStateShard.NO_TEXTURE;
import static net.minecraft.client.renderer.RenderStateShard.RENDERTYPE_LINES_SHADER;
import static net.minecraft.client.renderer.RenderStateShard.RENDERTYPE_TEXT_BACKGROUND_SEE_THROUGH_SHADER;
import static net.minecraft.client.renderer.RenderStateShard.TRANSLUCENT_TRANSPARENCY;
import static net.minecraft.client.renderer.RenderStateShard.VIEW_OFFSET_Z_LAYERING;
import static net.minecraft.client.renderer.RenderType.create;

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
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_TEXT_BACKGROUND_SEE_THROUGH_SHADER)
                    .setTextureState(NO_TEXTURE)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    /**
     * Can be used to draw lines that have no depth sorting.
     */
    private static final RenderType LINES_NO_DEPTH = create(
            "nodepth-lines",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_LINES_SHADER)
                    .setLineState(DEFAULT_LINE)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .createCompositeState(false)
    );

    public static RenderType textBackgroundSeeThroughWithDepth() {
        return TEXT_BACKGROUND_SEE_THROUGH_WITH_DEPTH;
    }

    public static RenderType linesNoDepth() {
        return LINES_NO_DEPTH;
    }
}
