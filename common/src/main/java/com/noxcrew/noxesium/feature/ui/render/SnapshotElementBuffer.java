package com.noxcrew.noxesium.feature.ui.render;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.platform.GlStateManager;
import java.nio.ByteBuffer;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL44;

/**
 * An element buffer that also has an attached PBO
 * and bound buffer so it can be snapshot.
 */
public class SnapshotElementBuffer extends ElementBuffer {

    private int currentIndex = 0;
    private int validPbos = 0;
    private boolean pboReady;
    private GpuBuffer[] pbos;
    private ByteBuffer[] buffers;
    private GpuFence fence;

    /**
     * Returns whether the buffer is ready for a snapshot.
     */
    public boolean canSnapshot() {
        return !pboReady && fence == null && validPbos < 2;
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

    @Override
    protected void configure(int width, int height) {
        super.configure(width, height);

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
    }

    @Override
    public void close() {
        super.close();

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
    }
}
