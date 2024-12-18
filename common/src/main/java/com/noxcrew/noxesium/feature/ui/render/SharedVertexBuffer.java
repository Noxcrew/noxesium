package com.noxcrew.noxesium.feature.ui.render;

import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.noxcrew.noxesium.feature.CustomCoreShaders;
import com.noxcrew.noxesium.feature.ui.render.api.BlendState;
import com.noxcrew.noxesium.feature.ui.render.api.BlendStateHook;
import com.noxcrew.noxesium.feature.ui.render.api.BufferData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CoreShaders;
import org.joml.Matrix4f;

import java.io.Closeable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Stores a shared vertex buffer for running the blit shader to copy a render target to the screen.
 * The vertex buffer is always the same so we re-use it. Vanilla creates them every frame when it
 * uses the shader but we'll reuse them.
 */
public class SharedVertexBuffer implements Closeable {

    private static final int MAX_SAMPLERS = 8;
    private static final Matrix4f NULL_MATRIX = new Matrix4f();

    // Sets a hook which controls the current blend state hook
    public static BlendStateHook blendStateHook = null;

    // Whether rebinding the current render target is allowed
    public static boolean allowRebindingTarget = true;

    // Set to true whenever the blend state is being cleared.
    public static boolean ignoreBlendStateHook = false;

    private static VertexBuffer buffer;
    private static final AtomicBoolean configuring = new AtomicBoolean(false);

    /**
     * Resets any cached values.
     */
    public static void reset() {
        blendStateHook = null;
        allowRebindingTarget = true;
        ignoreBlendStateHook = false;
    }

    /**
     * Binds this vertex buffer.
     */
    public static void bind() {
        buffer.bind();
    }

    /**
     * Performs buffer rendering for the given buffer texture ids.
     */
    public static void draw(List<BufferData> textureIds) {
        if (textureIds.isEmpty()) return;

        // Create the vertex buffer if it's not already been made
        create();
        if (buffer == null) return;

        // Set the texture and draw the buffer using the render texture
        // We can safely disable and re-enable the depth test because we know
        // the depth test is on through all UI rendering. We want to nicely
        // set the blending state back to what it was though to avoid causing
        // issues with other components.
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        // Cache the current blend state so we can return to it
        var originalBlendState = BlendState.snapshot();

        // Set up the correct blending properties, this matches
        // the default way that transparent elements are rendered
        // https://stackoverflow.com/questions/2171085/opengl-blending-with-previous-contents-of-framebuffer
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // Set up the correct shaders and color
        var shader = Objects.requireNonNull(RenderSystem.setShader(CustomCoreShaders.BLIT_SCREEN_MULTIPLE), "Blit shader not loaded");
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Bind the vertex shader
        SharedVertexBuffer.bind();

        // Perform drawing of all buffers at once
        bind();

        // Set up the uniform with the amount of buffers
        var index = 0;
        var uniform = Objects.requireNonNull(shader.getUniform("SamplerCount"));
        for (var texture : textureIds) {
            if (texture.state() == null) {
                // Add this texture to the textures
                RenderSystem.setShaderTexture(index++, texture.textureId());

                // If we've reached the maximum amount we run a draw
                if (index == MAX_SAMPLERS) {
                    uniform.set(MAX_SAMPLERS);
                    buffer.drawWithShader(NULL_MATRIX, NULL_MATRIX, shader);
                    index = 0;
                }
            } else {
                // End the previous wave of buffers and draw this buffer
                // on its own with a different blending state
                if (index > 0) {
                    for (var id = index; id < MAX_SAMPLERS; id++) {
                        RenderSystem.setShaderTexture(id, -1);
                    }
                    uniform.set(index);
                    buffer.drawWithShader(NULL_MATRIX, NULL_MATRIX, shader);
                    index = 0;
                }

                // Now draw this layer itself
                texture.state().apply();

                // Change the current shader, bind the texture, and run it
                shader = Objects.requireNonNull(RenderSystem.setShader(CoreShaders.BLIT_SCREEN), "Regular blit shader not loaded");
                shader.bindSampler("InSampler", texture.textureId());
                buffer.drawWithShader(NULL_MATRIX, NULL_MATRIX, shader);

                // Set it back to the regular blending function
                shader = Objects.requireNonNull(RenderSystem.setShader(CustomCoreShaders.BLIT_SCREEN_MULTIPLE), "Blit shader not loaded");
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }
        }

        // Draw any remaining buffers as well
        if (index > 0) {
            for (var id = index; id < MAX_SAMPLERS; id++) {
                RenderSystem.setShaderTexture(id, -1);
            }
            uniform.set(index);
            buffer.drawWithShader(NULL_MATRIX, NULL_MATRIX, shader);
        }

        VertexBuffer.unbind();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Restore the old blend state directly
        originalBlendState.apply();
    }

    /**
     * Creates the buffer if it does not yet exist.
     */
    private static void create() {
        if (buffer == null) {
            if (configuring.compareAndSet(false, true)) {
                try {
                    // Close the old buffer
                    if (buffer != null) {
                        buffer.close();
                        buffer = null;
                    }

                    // Create a buffer that just has geometry to render the texture onto the entire screen
                    var buffer = new VertexBuffer(BufferUsage.STATIC_WRITE);
                    buffer.bind();

                    // Do not use the main tesselator as it gets cleared at the end of frames.
                    var builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
                    builder.addVertex(0f, 0f, 0f);
                    builder.addVertex(1f, 0f, 0f);
                    builder.addVertex(1f, 1f, 0f);
                    builder.addVertex(0f, 1f, 0f);
                    buffer.upload(builder.build());

                    // Assign the buffer instance last!
                    SharedVertexBuffer.buffer = buffer;
                } finally {
                    configuring.set(false);
                }
            }
        }
    }

    @Override
    public void close() {
        if (buffer != null) {
            buffer.close();
            buffer = null;
        }
    }

    /**
     * Rebinds the main render target.
     */
    public static void rebindMainRenderTarget() {
        RenderSystem.assertOnRenderThread();

        // Bind the main render target to replace this target,
        // we do not need to unbind this buffer first as it
        // gets replaced by running bindWrite.
        allowRebindingTarget = true;
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
    }
}
