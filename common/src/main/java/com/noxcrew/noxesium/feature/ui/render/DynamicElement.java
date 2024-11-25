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

/**
 * Manages a buffer and its current dynamic fps.
 */
public class DynamicElement implements Closeable, BlendStateHook {

    // Stores whether any elements were drawn.
    public static boolean elementsWereDrawn = false;

    public static final BlendState DEFAULT_BLEND_STATE = BlendState.standard();
    public static final BlendState GLINT_BLEND_STATE = BlendState.glint();

    private final List<ElementBuffer> buffers = new ArrayList<>();
    private GuiGraphics guiGraphics;

    private boolean bufferZeroInvalid = false;
    private boolean needsRedraw = true;
    private boolean canCheck = true;

    private int renders = 0;
    private long nextCheck = System.nanoTime() + 1000000000;
    private int lastFps = 0;
    private long nextRender = -1;
    private int bufferIndex = 0;

    private boolean movementDirection = false;
    private int matches = 0;
    private long lastChange = System.currentTimeMillis();

    /**
     * The current fps at which we re-render the UI elements.
     */
    private double renderFps = NoxesiumMod.getInstance().getConfig().maxUiFramerate;

    public DynamicElement() {
        // Create a first buffer
        buffers.add(new ElementBuffer());
    }

    /**
     * Resets the display fps back to the maximum.
     */
    private void resetToMax() {
        if (!movementDirection) return;

        renderFps = NoxesiumMod.getInstance().getConfig().maxUiFramerate;
        movementDirection = false;
        matches = 0;
        lastChange = System.currentTimeMillis();
    }

    /**
     * Request an immediate redraw for this element.
     */
    public void redraw() {
        needsRedraw = true;
    }

    /**
     * Returns whether all buffers are empty.
     */
    public boolean isEmpty() {
        return bufferZeroInvalid;
    }

    /**
     * Returns whether there is at least one non-empty buffer.
     */
    public boolean isNotEmpty() {
        return !bufferZeroInvalid;
    }

    /**
     * The current frame rate of this group.
     */
    public int renderFramerate() {
        return (int) Math.floor(renderFps);
    }

    /**
     * Returns the amount of buffers used by this element.
     */
    public int buffers() {
        return buffers.size();
    }

    /**
     * Returns the true frame rate, the amount
     * of times this element was rendered the last
     * second.
     */
    public int framerate() {
        while (System.nanoTime() >= nextCheck) {
            nextCheck = System.nanoTime() + 1000000000;
            lastFps = renders;
            renders = 0;
        }
        return lastFps;
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
        if (isNotEmpty()) {
            for (var buffer : this.buffers) {
                if (buffer.isInvalid()) continue;
                buffers.add(new BufferData(buffer.getTextureId(), buffer.getBlendState()));
            }
        }
    }

    /**
     * Returns whether this element is ready to be considered
     * for group merging/joining.
     */
    public boolean isReady() {
        if (needsRedraw) return false;
        if (isNotEmpty()) {
            for (var buffer : buffers) {
                if (!buffer.hasValidPBO()) {
                    return false;
                }
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
     *
     * Determined by if we are currently stuck at max fps, and it's been
     * at least a second.
     */
    public boolean isOftenChanging() {
        return !movementDirection && (System.currentTimeMillis() - lastChange) >= 2000 && matches <= 20;
    }

    /**
     * Returns whether this element is eligible for merging.
     */
    public boolean isMergeable() {
        return movementDirection && (System.currentTimeMillis() - lastChange) >= 2000;
    }

    /**
     * Process recently taken snapshots to determine changes.
     */
    public void tick() {
        // Determine if all buffers are the same,
        // return the entire method if any buffer is not ready.
        var verdict = true;
        if (isNotEmpty()) {
            for (var buffer : buffers) {
                // Process the snapshots
                var snapshots = buffer.snapshots();
                if (snapshots == null) return;
                if (!compare(snapshots[0], snapshots[1])) {
                    verdict = false;
                }
            }
        }

        if (movementDirection) {
            // This means we are lowering fps, if anything changes we go back.
            if (verdict) {
                var max = NoxesiumMod.getInstance().getConfig().maxUiFramerate;
                renderFps = Math.max(1, Math.min(max, max * (1.0 - (double) ((System.currentTimeMillis() - lastChange) / 10000))));
            } else {
                resetToMax();
            }
        } else {
            // This means we are trying to lower fps as we are currently at the maximum.
            if (verdict) {
                matches = Math.min(200, matches + 1);
            } else {
                matches = Math.max(0, matches - 5);
            }

            // If we've reached 150 matches we
            // try to start building down!
            if (matches >= 150) {
                movementDirection = true;
                lastChange = System.currentTimeMillis();
            }
        }

        // Request new PBO's from all buffers
        if (isNotEmpty()) {
            for (var buffer : buffers) {
                buffer.requestNewPBO();
            }
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
        if (!needsRedraw && !canCheck && (renderFps <= 20 || nextRender > nanoTime)) return false;
        needsRedraw = false;
        renders++;

        // Set the next render time
        nextRender = nanoTime + (long) Math.floor(((1 / renderFps) * 1000000000));

        // Bind the first buffer, abort is something goes wrong
        this.guiGraphics = guiGraphics;
        if (!getBuffer(bufferIndex = 0).bind(guiGraphics)) return false;

        // Draw the layers onto the buffer while capturing the blending state
        elementsWereDrawn = false;
        SharedVertexBuffer.blendStateHook = this;
        draw.run();

        // Actually render things to this buffer
        guiGraphics.flush();
        SharedVertexBuffer.blendStateHook = null;

        // Remove any remaining buffers, if no elements were drawn onto the last one
        // we also remove that. Always keep at least 1 buffer otherwise we re-create
        // them too often.
        var firstUnusedBufferIndex = elementsWereDrawn ? bufferIndex + 1 : bufferIndex;
        for (var index = Math.max(1, firstUnusedBufferIndex); index < buffers.size(); index++) {
            buffers.remove(index).close();
        }
        bufferZeroInvalid = firstUnusedBufferIndex == 0;

        // Run PBO snapshot creation logic only if we want to run a check
        if (canCheck) {
            if (isNotEmpty()) {
                for (var buffer : buffers) {
                    if (buffer.canSnapshot()) {
                        buffer.snapshot();
                    }
                }
            }
            canCheck = false;
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
    private boolean compare(ByteBuffer first, ByteBuffer second) {
        return first.mismatch(second) == -1;
    }
}
