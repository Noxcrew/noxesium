package com.noxcrew.noxesium.feature.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.minecraft.client.Minecraft.ON_OSX;

/**
 * Holds all information about a buffered piece of UI.
 * <p>
 * Heavily inspired by <a href="https://github.com/tr7zw/Exordium">Exordium</a>!
 * <p>
 * The big difference in this implementation though is more detail in how
 * we determine when to redraw. Instead of being based on a fixed framerate
 * per element we instead tackle the problem directly and cache everything that
 * we know cannot change unless we cleared the cache.
 */
public class ScreenBuffer implements Closeable {

    private RenderTarget target;
    private VertexBuffer buffer;
    private int screenWidth;
    private int screenHeight;

    private final AtomicBoolean configuring = new AtomicBoolean(false);

    /**
     * Indicates that the buffer is valid.
     */
    public boolean isValid() {
        return buffer != null;
    }

    /**
     * Resizes this buffer to the given width and height.
     */
    public boolean resize(Window window) {
        RenderSystem.assertOnRenderThread();

        var width = window.getWidth();
        var height = window.getHeight();
        var screenWidth = window.getGuiScaledWidth();
        var screenHeight = window.getGuiScaledHeight();

        if (target == null || target.width != width || target.height != height || this.screenWidth != screenWidth || this.screenHeight != screenHeight) {
            if (configuring.compareAndSet(false, true)) {
                try {
                    // Close the old buffer
                    if (buffer != null) {
                        buffer.close();
                        buffer = null;
                    }

                    // Create a single texture that stretches the entirety of the buffer
                    var buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
                    buffer.bind();

                    var builder = new BufferBuilder(4);
                    builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                    builder.vertex(0.0f, screenHeight, -90.0f).uv(0.0f, 0.0f).endVertex();
                    builder.vertex(screenWidth, screenHeight, -90.0f).uv(1.0f, 0.0f).endVertex();
                    builder.vertex(screenWidth, 0.0f, -90.0f).uv(1.0f, 1.0f).endVertex();
                    builder.vertex(0.0f, 0.0f, -90.0f).uv(0.0f, 1.0f).endVertex();
                    buffer.upload(builder.end());

                    if (target == null) {
                        target = new TextureTarget(width, height, true, ON_OSX);
                    } else {
                        target.resize(width, height, ON_OSX);
                    }

                    // Assign the buffer instance last!
                    this.screenWidth = screenWidth;
                    this.screenHeight = screenHeight;
                    this.buffer = buffer;
                } finally {
                    configuring.set(false);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the render target of this buffer.
     */
    public RenderTarget getTarget() {
        return target;
    }

    /**
     * Returns the texture id of this buffer.
     */
    public int getTextureId() {
        return target.getColorTextureId();
    }

    /**
     * Draws this buffer to the screen.
     */
    public void draw() {
        if (buffer == null) return;

        // Set the texture and draw the buffer using the render texture
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, getTextureId());
        buffer.bind();
        buffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void close() {
        if (buffer != null) {
            buffer.close();
            buffer = null;
        }
        if (target != null) {
            target.destroyBuffers();
            target = null;
        }
    }
}
