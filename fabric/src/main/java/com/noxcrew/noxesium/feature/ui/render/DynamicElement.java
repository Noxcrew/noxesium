package com.noxcrew.noxesium.feature.ui.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.ui.render.api.BlendState;
import com.noxcrew.noxesium.feature.ui.render.api.BlendStateHook;
import com.noxcrew.noxesium.feature.ui.render.api.BufferData;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.opengl.GL14;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages a buffer and its current dynamic fps.
 */
public class DynamicElement implements Closeable, BlendStateHook {

    // Stores whether any elements were drawn.
    public static boolean elementsWereDrawn = false;

    public static final BlendState DEFAULT_BLEND_STATE = BlendState.standard();
    public static final BlendState GLINT_BLEND_STATE = BlendState.glint();

    private final List<ElementBuffer> buffers = new ArrayList<>();
    private boolean bufferEmpty = false;
    private boolean needsRedraw = true;
    private boolean allBuffersEmpty = true;
    private boolean canCheck = true;
    private long nextRender = -1;
    private int failedCheckCount = 0;
    private int bufferIndex = 0;
    private GuiGraphics guiGraphics;

    /**
     * The current fps at which we re-render the UI elements.
     */
    private double renderFps = NoxesiumMod.getInstance().getConfig().maxUiFramerate;

    public DynamicElement() {
        // Create a first buffer
        buffers.add(new ElementBuffer());
    }

    /**
     * Request an immediate redraw for this element.
     */
    public void redraw() {
        needsRedraw = true;
    }

    /**
     * The current frame rate of this group.
     */
    public int renderFramerate() {
        return (int) Math.floor(renderFps);
    }

    /**
     * Returns whether any buffer is invalid.
     */
    public boolean isInvalid() {
        // If any buffer is invalid we return true!
        for (var buffer : buffers) {
            if (buffer.isInvalid()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the texture ids for this buffer to the given list.
     */
    public void submitTextureIds(List<BufferData> buffers) {
        if (bufferEmpty) return;
        for (var buffer : this.buffers) {
            if (buffer.isInvalid()) continue;
            buffers.add(new BufferData(buffer.getTextureId(), buffer.getBlendState()));
        }
    }

    /**
     * Returns whether this element is ready to be considered
     * for group merging/joining.
     */
    public boolean isReady() {
        if (needsRedraw) return false;
        for (var buffer : buffers) {
            if (!buffer.hasValidPBO()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Indicates that a check should run the very next frame.
     */
    public void requestCheck() {
        canCheck = true;
    }

    /**
     * Triggers an update of the render framerate.
     */
    public void updateRenderFramerate() {
        // Just set the render fps back to the max framerate
        // whenever the maximum framerate has changed.
        renderFps = NoxesiumMod.getInstance().getConfig().maxUiFramerate;
    }

    /**
     * Returns whether this element is often changing. Used to determine
     * when it should be split up this buffer.
     */
    public boolean isOftenChanging() {
        return failedCheckCount >= 20;
    }

    /**
     * Process recently taken snapshots to determine changes.
     */
    public void tick() {
        // Determine if all buffers are the same,
        // return the entire method if any buffer is not ready.
        var verdict = true;
        for (var buffer : buffers) {
            // Process the snapshots
            var snapshots = buffer.snapshots();
            if (snapshots == null) return;

            var empty = buffer.emptySnapshots();
            if (!compare(empty, snapshots[0], snapshots[1])) {
                verdict = false;
            }
        }

        if (verdict) {
            // The frames matched, slow down the rendering! We can go down to 0
            // which means we update every client tick which is when the server
            // may have changed.
            renderFps = Math.max(0, Math.min(renderFps / 2, renderFps - 5));
            failedCheckCount = Math.min(-1, failedCheckCount - 1);
        } else {
            // The frames did not match, back to full speed!
            renderFps = NoxesiumMod.getInstance().getConfig().maxUiFramerate;
            failedCheckCount = Math.max(1, failedCheckCount + 1);
        }

        // Request new PBO's from all buffers
        for (var buffer : buffers) {
            buffer.requestNewPBO();
        }
    }

    /**
     * Updates the current state of this element.
     */
    public boolean update(long nanoTime, GuiGraphics guiGraphics, Runnable draw) {
        // Always start by awaiting the GPU fence
        for (var buffer : buffers) {
            buffer.awaitFence();
        }

        // Determine if we are at the next render threshold yet, otherwise
        // we wait until we have reached it

        // Initialize the value if it's missing
        if (nextRender == -1) {
            nextRender = nanoTime;
        }

        // Skip the update until we reach the next render time
        if (!needsRedraw && !canCheck && nextRender > nanoTime) return false;
        needsRedraw = false;

        // Set the next render time
        nextRender = nanoTime + (long) Math.floor(((1 / renderFps) * 1000000000));

        // Bind the first buffer, abort is something goes wrong
        this.guiGraphics = guiGraphics;
        if (!getBuffer(bufferIndex = 0).bind(guiGraphics)) return false;

        // Draw the layers onto the buffer while capturing the blending state
        elementsWereDrawn = false;
        allBuffersEmpty = true;
        SharedVertexBuffer.blendStateHook = this;
        draw.run();

        // Actually render things to this buffer
        guiGraphics.flush();
        SharedVertexBuffer.blendStateHook = null;

        // Remove any remaining buffers
        for (var index = (bufferIndex + 1); index < buffers.size(); index++) {
            buffers.remove(index).close();
        }

        // Nothing was drawn, this layer does not contain anything!
        bufferEmpty = !elementsWereDrawn && allBuffersEmpty;

        // Run PBO snapshot creation logic only if we want to run a check
        if (canCheck) {
            for (var buffer : buffers) {
                if (buffer.canSnapshot()) {
                    buffer.snapshot(bufferEmpty);
                    canCheck = false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the buffer with the given index.
     */
    private ElementBuffer getBuffer(int index) {
        if (index < buffers.size()) {
            return buffers.get(index);
        }
        if (index == buffers.size()) {
            var newBuffer = new ElementBuffer();
            buffers.add(newBuffer);
            return newBuffer;
        }
        throw new IllegalArgumentException("Cannot get a buffer at an invalid index");
    }

    @Override
    public boolean changeState(boolean newValue) {
        // Ignore any enabling of blending as blending is already enabled
        if (newValue) return false;

        // If blending is turned off at any point we don't need to fork the buffer, we just need to temporarily change how
        // we approach blending. We want to copy the RGB normally but set the alpha to a static value of 255. For this we
        // use the constant color system.
        SharedVertexBuffer.ignoreBlendStateHook = true;
        GlStateManager._blendFuncSeparate(
                // Copy normal colors directly
                GL14.GL_ONE,
                GL14.GL_ZERO,
                // Use the constant color's alpha value
                // for the resulting pixel in the buffer,
                // since the buffer starts out being entirely
                // transparent this puts a fully opaque
                // pixels at any pixel we draw to when not
                // blending.
                GL14.GL_CONSTANT_ALPHA,
                GL14.GL_ZERO
        );
        SharedVertexBuffer.ignoreBlendStateHook = false;

        // Don't let the blending disable go through
        return true;
    }

    @Override
    public boolean changeFunc(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) {
        var isNormal = DEFAULT_BLEND_STATE.matches(srcRgb, dstRgb, srcAlpha, dstAlpha) ||
                // We allow glint states as this is one that specifically applies edits to an existing
                // item that was just rendered in the same buffer.
                GLINT_BLEND_STATE.matches(srcRgb, dstRgb, srcAlpha, dstAlpha);

        // If we are currently in a buffer with any custom blend
        // state we go back to a normal buffer!
        if (getBuffer(bufferIndex).getBlendState() != null && isNormal) {
            // Re-enable blending and let it go through
            SharedVertexBuffer.ignoreBlendStateHook = true;
            GlStateManager._enableBlend();
            SharedVertexBuffer.ignoreBlendStateHook = false;

            var buffer = getBuffer(elementsWereDrawn ? ++bufferIndex : bufferIndex);
            buffer.bind(guiGraphics);
            buffer.updateBlendState(null);
            if (elementsWereDrawn) allBuffersEmpty = false;
            elementsWereDrawn = false;
            return false;
        }

        // Ignore any changes that do not actually change from the default blend state, or
        // if they are specific types of blend states that are permitted.
        if (isNormal) return false;

        // Update the buffer to the next one
        var buffer = getBuffer(elementsWereDrawn ? ++bufferIndex : bufferIndex);
        buffer.bind(guiGraphics);
        buffer.updateBlendState(BlendState.from(srcRgb, dstRgb, srcAlpha, dstAlpha));
        if (elementsWereDrawn) allBuffersEmpty = false;
        elementsWereDrawn = false;

        // Change the actual blending by disabling it completely which is slightly faster
        // than using a different blending function that just copies it directly (which is
        // what we want, we want to do the blending later)
        SharedVertexBuffer.ignoreBlendStateHook = true;
        GlStateManager._disableBlend();
        SharedVertexBuffer.ignoreBlendStateHook = false;
        return true;
    }

    @Override
    public void close() {
        for (var buffer : buffers) {
            buffer.close();
        }
        buffers.clear();
    }

    /**
     * Compares two frame snapshots.
     */
    private boolean compare(boolean[] empty, ByteBuffer first, ByteBuffer second) {
        // If both are empty, they match.
        if (empty[0] && empty[1]) return true;

        // If one is empty but the other isn't, they don't match.
        if (empty[0] || empty[1]) return false;

        return first.mismatch(second) == -1;
    }
}
