package com.noxcrew.noxesium.feature.ui.render.buffer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.noxcrew.noxesium.feature.ui.BufferHelper;
import com.noxcrew.noxesium.feature.ui.render.SharedVertexBuffer;
import com.noxcrew.noxesium.feature.ui.render.api.BlendState;
import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper around a RenderTarget used for capturing rendered UI elements
 * and re-using them.
 * <p>
 * Inspired by <a href="https://github.com/tr7zw/Exordium">Exordium</a>
 */
public class ElementBuffer implements Closeable {

    private final AtomicBoolean configuring = new AtomicBoolean(false);

    private RenderTarget target;
    private BlendState blendState;

    /**
     * Binds this buffer to the render target, replacing any previous target.
     */
    public boolean bind(GuiGraphics guiGraphics) {
        RenderSystem.assertOnRenderThread();

        // Bind various things if this is the first frame buffer we are switching into
        BufferHelper.bind(guiGraphics);

        // Before binding we want to resize this buffer if necessary
        SharedVertexBuffer.allowRebindingTarget = true;
        resize();
        SharedVertexBuffer.allowRebindingTarget = false;

        // If something went wrong we abort!
        if (isInvalid()) return false;

        // Bind the render target for writing
        SharedVertexBuffer.allowRebindingTarget = true;
        target.bindWrite(true);
        SharedVertexBuffer.allowRebindingTarget = false;

        // Clear the contents of the render target while keeping it bound
        GlStateManager._clearColor(0, 0, 0, 0);
        GlStateManager._clearDepth(1.0);
        GlStateManager._clear(16384 | 256);
        return true;
    }

    /**
     * Resizes this buffer to fit the game window.
     */
    private void resize() {
        var window = Minecraft.getInstance().getWindow();
        var width = window.getWidth();
        var height = window.getHeight();

        if (target == null || target.width != width || target.height != height) {
            if (configuring.compareAndSet(false, true)) {
                try {
                    configure(width, height);
                } finally {
                    configuring.set(false);
                }
            }
        }
    }

    /**
     * Configures this buffer.
     */
    protected void configure(int width, int height) {
        if (target == null) {
            // This constructor internally runs resize! True indicates that we want
            // a depth buffer to be created as well.
            target = new TextureTarget(width, height, true);
        } else {
            target.resize(width, height);
        }
    }

    /**
     * Returns the texture id of this element.
     */
    public int getTextureId() {
        return target.getColorTextureId();
    }

    /**
     * Returns whether the buffer is invalid.
     */
    public boolean isInvalid() {
        return target == null;
    }

    /**
     * Updates the blend state associated with this buffer.
     */
    public void updateBlendState(@Nullable BlendState blendState) {
        this.blendState = blendState;
    }

    /**
     * Returns the associated blend state of this buffer.
     */
    @Nullable
    public BlendState getBlendState() {
        return blendState;
    }

    @Override
    public void close() {
        if (target != null) {
            target.destroyBuffers();
            target = null;
        }
    }
}
