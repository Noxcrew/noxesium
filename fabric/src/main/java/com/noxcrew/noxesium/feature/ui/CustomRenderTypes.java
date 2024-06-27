package com.noxcrew.noxesium.feature.ui;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

import static net.minecraft.client.renderer.RenderStateShard.COLOR_WRITE;
import static net.minecraft.client.renderer.RenderStateShard.LEQUAL_DEPTH_TEST;
import static net.minecraft.client.renderer.RenderStateShard.LIGHTMAP;
import static net.minecraft.client.renderer.RenderStateShard.NO_TEXTURE;
import static net.minecraft.client.renderer.RenderStateShard.RENDERTYPE_TEXT_BACKGROUND_SEE_THROUGH_SHADER;
import static net.minecraft.client.renderer.RenderStateShard.TRANSLUCENT_TRANSPARENCY;
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

    public static RenderType textBackgroundSeeThroughWithDepth() {
        return TEXT_BACKGROUND_SEE_THROUGH_WITH_DEPTH;
    }
}
