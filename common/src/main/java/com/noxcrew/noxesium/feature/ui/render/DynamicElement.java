package com.noxcrew.noxesium.feature.ui.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.ui.BufferHelper;
import com.noxcrew.noxesium.feature.ui.render.api.PerSecondRepeatingTask;
import com.noxcrew.noxesium.feature.ui.render.buffer.ElementBuffer;
import com.noxcrew.noxesium.feature.ui.render.buffer.SnapshotableElementBuffer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Manages a buffer and its current dynamic fps.
 */
public class DynamicElement extends Element {

    private final List<SnapshotableElementBuffer> buffers = new ArrayList<>();
    private final PerSecondRepeatingTask nextRender =
            new PerSecondRepeatingTask(NoxesiumMod.getInstance().getConfig().maxUiFramerate);

    private boolean movementDirection = false;
    private boolean hasBufferLayoutChanged = false;
    private boolean hasRedrawnRecently = false;
    private long lastChange = System.currentTimeMillis();
    private int matches = 0;

    /**
     * Returns the repeating task used for rendering.
     */
    public PerSecondRepeatingTask getRenderTask() {
        return nextRender;
    }

    /**
     * Resets the display fps back to the maximum.
     */
    public void resetToMax() {
        nextRender.changeFrequency(NoxesiumMod.getInstance().getConfig().maxUiFramerate);

        if (!movementDirection) return;
        movementDirection = false;
        matches = 0;
        lastChange = System.currentTimeMillis();
    }

    /**
     * Returns the percentage of matching frames.
     */
    public int matchRate() {
        return (int) ((((double) matches) / 60d) * 100d);
    }

    /**
     * Returns the amount of buffers used by this element.
     */
    public int buffers() {
        return buffers.size();
    }

    /**
     * Returns whether this element is ready to be considered
     * for group merging/joining.
     */
    public boolean isReady() {
        if (needsRedraw()) return false;
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
     * Returns whether this element is often changing. Used to determine
     * when it should be split up this buffer.
     * <p>
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
        var verdict = !hasBufferLayoutChanged;
        if (isNotEmpty()) {
            for (var buffer : buffers) {
                if (buffer instanceof SnapshotableElementBuffer pboBuffer) {
                    // Process the snapshots
                    var snapshots = pboBuffer.snapshots();
                    if (snapshots == null) return;
                    if (!snapshots[0].equals(snapshots[1])) {
                        verdict = false;
                        break;
                    }
                }
            }
        }

        // Update after we are committed to this tick
        hasBufferLayoutChanged = false;

        // This means we are lowering fps, if anything changes more than thrice we go back.
        if (verdict) {
            matches = Math.min(60, matches + 1);
        } else {
            matches = Math.max(0, matches - 3);
        }

        if (movementDirection) {
            var max = NoxesiumMod.getInstance().getConfig().maxUiFramerate;
            nextRender.changeFrequency(Math.clamp(
                    Math.max(
                            max * (1.0 - ((double) (System.currentTimeMillis() - lastChange) / 10000d)),
                            max * ((double) (60 - matches) / 10d)),
                    0,
                    max));

            // If matches falls too far
            if (matches <= 40) {
                resetToMax();
            }
        } else {
            // If we've reached 60 matches we
            // try to start building down!
            if (matches >= 60) {
                movementDirection = true;
                lastChange = System.currentTimeMillis();
            }
        }

        // Request new PBOs from all buffers
        if (isNotEmpty()) {
            for (var buffer : buffers) {
                buffer.requestNewPBO();
            }
        }
    }

    /**
     * Tries to make a snapshots of all buffers that can if
     * this frame is useful.
     */
    public void trySnapshot() {
        // Only snapshot if we've redrawn recently
        if (buffers.isEmpty() || !hasRedrawnRecently) return;

        // Return if any buffer can't snapshot
        for (var buffer : buffers) {
            if (!buffer.canSnapshot()) return;
        }

        // Update after we are committed
        hasRedrawnRecently = false;

        // Try to snapshot on every buffer and then unbind the main target
        for (var buffer : buffers) {
            buffer.snapshot();
        }
        // SharedVertexBuffer.rebindMainRenderTarget();
    }

    @Override
    public boolean update(long nanoTime, GuiGraphics guiGraphics, Runnable draw) {
        // Always start by awaiting the GPU fence and updating if the PBO is ready
        // for the next tick!
        for (var buffer : buffers) {
            buffer.awaitFence();
        }

        var result = super.update(nanoTime, guiGraphics, draw);
        if (result) {
            hasRedrawnRecently = true;
        }
        return result;
    }

    @Override
    public boolean shouldRedraw(long nanoTime) {
        return nextRender.canInvoke(nanoTime);
    }

    @Override
    public List<ElementBuffer> getBuffers() {
        return (List<ElementBuffer>) (List<?>) buffers;
    }

    @Override
    protected void onBufferRemoved(ElementBuffer buffer) {
        super.onBufferRemoved(buffer);
        hasBufferLayoutChanged = true;
    }

    @Override
    public ElementBuffer createBuffer() {
        hasBufferLayoutChanged = true;
        return new SnapshotableElementBuffer();
    }
}
