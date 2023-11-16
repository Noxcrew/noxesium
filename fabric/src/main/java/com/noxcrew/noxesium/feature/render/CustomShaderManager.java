package com.noxcrew.noxesium.feature.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;

/**
 * Defines custom shader types.
 */
public class CustomShaderManager {

    private static ShaderInstance positionTexBlendShader;

    public static ShaderInstance getPositionTexBlendShader() {
        return positionTexBlendShader;
    }

    public static void reloadShaders(GameRenderer gameRenderer, ResourceProvider resourceProvider) {
        positionTexBlendShader = gameRenderer.preloadShader(resourceProvider, "position_tex_blend", DefaultVertexFormat.POSITION_TEX);
    }
}
