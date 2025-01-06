package com.noxcrew.noxesium.feature.ui.render;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.noxcrew.noxesium.feature.ui.BufferHelper;
import com.noxcrew.noxesium.feature.ui.render.api.BlendState;
import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL44;

/**
 * A wrapper around a RenderTarget used for capturing rendered UI elements
 * and re-using them.
 * <p>
 * Inspired by <a href="https://github.com/tr7zw/Exordium">Exordium</a>
 */
public class ElementBuffer implements Closeable {

    private int currentIndex = 0;
    private int validPbos = 0;
    private boolean pboReady;
    private GpuBuffer[] pbos;
    private ByteBuffer[] buffers;
    private RenderTarget target;
    private GpuFence fence;
    private BlendState blendState;

    private final AtomicBoolean configuring = new AtomicBoolean(false);

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

    /**
     * Returns whether the buffer is ready for a snapshot.
     */
    public boolean canSnapshot() {
        return fence == null && validPbos < 2;
    }

    /**
     * Awaits the contents of the PBO being available.
     */
    public void awaitFence() {
        // Wait for actual data to be available
        if (fence == null || pbos == null || buffers == null) return;
        if (fence.awaitCompletion(0L)) {
            // Mark down that we've taken an PBO and prevent taking
            // another snapshot for now and we're ready to have them
            // compared.
            validPbos++;
            fence = null;
            pboReady = true;
        }
    }

    /**
     * Snapshots the current buffer contents to a PBO.
     */
    public void snapshot() {
        if (fence != null || pbos == null || buffers == null) return;

        // Flip which buffer we are drawing into
        if (currentIndex == 1) currentIndex = 0;
        else currentIndex = 1;

        // Bind the PBO to tell the GPU to read the frame buffer's
        // texture into it directly
        pbos[currentIndex].bind();

        // TODO This causes micro-stutters!
        var window = Minecraft.getInstance().getWindow();
        GL11.glReadPixels(0, 0, window.getWidth(), window.getHeight(), GL30.GL_BGRA, GL11.GL_UNSIGNED_BYTE, 0);

        // GetTexImage produces weird results sometimes, it doesn't seem to catch
        // the crosshair changing and thinks the scoreboard changes every tick.
        /*GL11.glGetTexImage(
                GL11.GL_TEXTURE_2D,
                0,
                GL30.GL_BGRA,
                GL11.GL_UNSIGNED_BYTE,
                0
        );*/

        // Unbind the PBO so it doesn't get modified afterwards
        GlStateManager._glBindBuffer(GL30.GL_PIXEL_PACK_BUFFER, 0);

        // Start waiting for the GPU to return the data
        fence = new GpuFence();
    }

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
                    // Create the PBO / re-size an existing buffer instance
                    if (pbos == null) {
                        pbos = new GpuBuffer[2];
                    }
                    if (buffers == null) {
                        buffers = new ByteBuffer[2];
                    }
                    for (var i = 0; i < 2; i++) {
                        if (pbos[i] == null) {
                            pbos[i] = new GpuBuffer(BufferType.PIXEL_PACK, BufferUsage.STREAM_READ, 0);
                        }
                        pbos[i].resize(width * height * 4);

                        if (buffers[i] == null) {
                            // Configure the buffer to have a persistent size so we can keep it bound permanently
                            var flags = GL30C.GL_MAP_READ_BIT | GL44.GL_MAP_PERSISTENT_BIT | GL44.GL_MAP_COHERENT_BIT;
                            GL44.glBufferStorage(GL30.GL_PIXEL_PACK_BUFFER, pbos[i].size, flags);

                            // Create a persistent buffer to the PBOs contents
                            buffers[i] = Preconditions.checkNotNull(
                                    GL30.glMapBufferRange(GL30.GL_PIXEL_PACK_BUFFER, 0, pbos[i].size, flags));
                        }
                    }

                    if (target == null) {
                        // This constructor internally runs resize! True indicates that we want
                        // a depth buffer to be created as well.
                        target = new TextureTarget(width, height, true);
                    } else {
                        target.resize(width, height);
                    }
                } finally {
                    configuring.set(false);
                }
            }
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
     * Returns the snapshots that were taken.
     */
    public ByteBuffer[] snapshots() {
        return pboReady ? buffers : null;
    }

    /**
     * Returns whether this buffer has at least one valid PBO.
     */
    public boolean hasValidPBO() {
        return validPbos > 0;
    }

    /**
     * Marks down that a new PBO should be updated.
     */
    public void requestNewPBO() {
        pboReady = false;
        validPbos--;
    }

    @Override
    public void close() {
        buffers = null;
        if (pbos != null) {
            for (var pbo : pbos) {
                pbo.close();
            }
            pbos = null;
        }
        if (fence != null) {
            fence.close();
            fence = null;
        }
        if (target != null) {
            target.destroyBuffers();
            target = null;
        }
    }
}
