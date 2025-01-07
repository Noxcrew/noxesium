package com.noxcrew.noxesium.feature.ui.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.noxcrew.noxesium.feature.ui.render.api.BlendState;
import com.noxcrew.noxesium.feature.ui.render.api.BlendStateHook;
import com.noxcrew.noxesium.feature.ui.render.api.BufferData;
import java.io.Closeable;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.opengl.GL14;

/**
 * The base for an element that manages a buffer.
 */
public abstract class Element implements Closeable, BlendStateHook {

    // Stores whether any elements were drawn.
    public static boolean elementsWereDrawn = false;

    public static final BlendState DEFAULT_BLEND_STATE = BlendState.standard();
    public static final BlendState GLINT_BLEND_STATE = BlendState.glint();

    private GuiGraphics guiGraphics;

    private boolean needsRedraw = true;
    private boolean empty = false;
    private boolean lastBlending = true;
    private int boundBufferIndex = -1;

    private final PerSecondTrackedValue updates = new PerSecondTrackedValue();
    private final PerSecondTrackedValue draws = new PerSecondTrackedValue();

    /**
     * Returns all buffers in this element.
     */
    public abstract List<ElementBuffer> getBuffers();

    /**
     * Creates a new buffer.
     */
    public abstract ElementBuffer createBuffer();

    /**
     * Whether the buffers should be re-drawn this time.
     */
    public abstract boolean shouldRedraw(long nanoTime);

    /**
     * Returns who often this element was drawn the past second.
     */
    public int getDrawsThisSecond() {
        return draws.get();
    }

    /**
     * Returns who often this element was updated the past second.
     */
    public int getUpdatesThisSecond() {
        return draws.get();
    }

    /**
     * Returns whether this element needs to be redrawn.
     */
    public boolean needsRedraw() {
        return needsRedraw;
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
        return empty;
    }

    /**
     * Returns whether there is at least one non-empty buffer.
     */
    public boolean isNotEmpty() {
        return !empty;
    }

    /**
     * Returns whether any buffer is invalid.
     */
    public boolean isInvalid() {
        // If any buffer is invalid we return true!
        for (var buffer : getBuffers()) {
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
            for (var buffer : getBuffers()) {
                if (buffer.isInvalid()) continue;
                buffers.add(new BufferData(buffer.getTextureId(), buffer.getBlendState()));
            }
        }
    }

    /**
     * Updates the current state of this element.
     */
    public boolean update(long nanoTime, GuiGraphics guiGraphics, Runnable draw) {
        this.updates.increment();
        if (!shouldRedraw(nanoTime)) return false;

        // Bind the first buffer, abort is something goes wrong
        this.guiGraphics = guiGraphics;
        this.boundBufferIndex = -1;

        var nextBuffer = getNextLayerBuffer();
        if (!nextBuffer.bind(guiGraphics)) return false;

        // Draw the layers onto the buffer while capturing the blending state
        elementsWereDrawn = false;
        lastBlending = true;
        SharedVertexBuffer.blendStateHook = this;
        draw.run();

        // Actually render things to this buffer
        guiGraphics.flush();
        SharedVertexBuffer.blendStateHook = null;

        // Remove any remaining buffers, if no elements were drawn onto the last one
        // we also remove that. Always keep at least 1 buffer otherwise we re-create
        // them too often.
        if (!elementsWereDrawn) {
            this.boundBufferIndex--;
        }
        this.needsRedraw = false;
        this.empty = !elementsWereDrawn && this.boundBufferIndex < 0;
        this.draws.increment();

        var buffers = getBuffers();
        for (var index = Math.max(1, this.boundBufferIndex + 1); index < buffers.size(); index++) {
            buffers.remove(index).close();
        }
        return true;
    }

    /**
     * Returns the buffer with the given index.
     */
    public ElementBuffer getBuffer(int index) {
        var buffers = getBuffers();
        if (index < buffers.size()) {
            return buffers.get(index);
        }
        throw new IllegalArgumentException("Cannot get a buffer at an invalid index");
    }

    /**
     * Returns the current target buffer that is bound and being drawn to.
     */
    @Nullable
    public ElementBuffer getTargetBuffer() {
        var buffers = getBuffers();
        if (boundBufferIndex < 0 || boundBufferIndex >= buffers.size()) {
            return null;
        }
        return buffers.get(boundBufferIndex);
    }

    /**
     * Returns the next layer buffer to draw to. If nothing was drawn
     * this re-uses the last buffer.
     */
    protected ElementBuffer getNextLayerBuffer() {
        var buffers = getBuffers();

        // Try to re-use the same layer if we didn't draw anything
        if (boundBufferIndex >= 0 && !elementsWereDrawn) {
            return buffers.get(boundBufferIndex);
        }

        // Emit a hook for dealing with the buffer being de-targeted
        var lastTarget = getTargetBuffer();
        if (lastTarget != null) {
            onBufferUntargeted(lastTarget);
        }

        // Try to get an existing buffer
        var target = ++boundBufferIndex;
        if (target >= 0 && target < buffers.size()) {
            return buffers.get(target);
        }

        // Create a new buffer to use!
        var newBuffer = createBuffer();
        buffers.add(newBuffer);
        return newBuffer;
    }

    /**
     * Called before [buffer] stops being the target buffer.
     */
    protected void onBufferUntargeted(ElementBuffer buffer) {}

    @Override
    public boolean changeState(boolean newValue) {
        // Ignore any changes if we're already in this state, but allow changing it to be enabled!
        if (newValue == lastBlending) return !newValue;
        lastBlending = newValue;

        if (newValue) {
            // Set blending back to the normal mode
            SharedVertexBuffer.ignoreBlendStateHook = true;
            DEFAULT_BLEND_STATE.apply();
            SharedVertexBuffer.ignoreBlendStateHook = false;
        } else {
            // If blending is turned off at any point we don't need to fork the buffer, we just need to temporarily
            // change how
            // we approach blending. We want to copy the RGB normally but set the alpha to a static value of 255. For
            // this we
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
                    GL14.GL_ZERO);
            SharedVertexBuffer.ignoreBlendStateHook = false;
        }

        // Don't let the blending disable go through
        return !newValue;
    }

    @Override
    public boolean changeFunc(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) {
        var isNormal = DEFAULT_BLEND_STATE.matches(srcRgb, dstRgb, srcAlpha, dstAlpha)
                ||
                // We allow glint states as this is one that specifically applies edits to an existing
                // item that was just rendered in the same buffer.
                GLINT_BLEND_STATE.matches(srcRgb, dstRgb, srcAlpha, dstAlpha);

        // If we are currently in a buffer with any custom blend
        // state we go back to a normal buffer!
        var target = getTargetBuffer();
        if (target != null && target.getBlendState() != null && isNormal) {
            // Re-enable blending and let it go through
            SharedVertexBuffer.ignoreBlendStateHook = true;
            GlStateManager._enableBlend();
            SharedVertexBuffer.ignoreBlendStateHook = false;

            var buffer = getNextLayerBuffer();
            buffer.bind(guiGraphics);
            buffer.updateBlendState(null);
            elementsWereDrawn = false;
            return false;
        }

        // Ignore any changes that do not actually change from the default blend state, or
        // if they are specific types of blend states that are permitted.
        if (isNormal) return false;

        // Update the buffer to the next one
        var buffer = getNextLayerBuffer();
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
        for (var buffer : getBuffers()) {
            buffer.close();
        }
        getBuffers().clear();
    }
}
