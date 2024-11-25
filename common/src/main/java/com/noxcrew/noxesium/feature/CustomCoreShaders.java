package com.noxcrew.noxesium.feature;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.resources.ResourceLocation;

/**
 * Defines a custom core shader used for buffers.
 */
public class CustomCoreShaders {
    public static final ShaderProgram BLIT_SCREEN_MULTIPLE = register("blit_screen_multiple", DefaultVertexFormat.BLIT_SCREEN);

    /**
     * Registers a new core shader.
     */
    private static ShaderProgram register(String name, VertexFormat format) {
        ShaderProgram shaderprogram = new ShaderProgram(ResourceLocation.fromNamespaceAndPath(NoxesiumReferences.NAMESPACE, "core/" + name), format, ShaderDefines.EMPTY);
        CoreShaders.getProgramsToPreload().add(shaderprogram);
        return shaderprogram;
    }
}
