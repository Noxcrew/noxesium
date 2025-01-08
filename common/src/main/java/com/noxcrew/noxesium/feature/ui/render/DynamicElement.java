package com.noxcrew.noxesium.feature.ui.render;

import com.noxcrew.noxesium.NoxesiumMod;
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

    private boolean canCheck = true;
    private boolean movementDirection = false;
    private boolean hasBufferLayoutChanged = false;
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
    public String matchRate() {
        return String.format("%.2f", ((double) matches) / 60d);
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
     * Indicates that a check should run the very next frame.
     */
    public void requestCheck() {
        canCheck = true;
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
        hasBufferLayoutChanged = false;
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
     * Tries to make a snapshot of the current buffer.
     */
    private void trySnapshot() {
        if (elementsWereDrawn && canCheck) {
            var target = getTargetBuffer();
            if (target instanceof SnapshotableElementBuffer pboBuffer) {
                pboBuffer.snapshot();
            }
        }
    }

    @Override
    public boolean update(long nanoTime, GuiGraphics guiGraphics, Runnable draw) {
        // Always start by awaiting the GPU fence and updating if the PBO is ready
        // for the next tick!
        for (var buffer : buffers) {
            buffer.awaitFence();
        }

        if (super.update(nanoTime, guiGraphics, draw)) {
            // Unset check once we're done with this frame, but
            // snapshot first!
            if (canCheck) {
                trySnapshot();
                canCheck = false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldRedraw(long nanoTime) {
        // If we can check we still increase the next render frame,
        // but we ignore its result!
        return nextRender.canInvoke(nanoTime) || canCheck;
    }

    @Override
    public List<ElementBuffer> getBuffers() {
        return (List<ElementBuffer>) (List<?>) buffers;
    }

    @Override
    protected void onBufferUntargeted(ElementBuffer buffer) {
        super.onBufferUntargeted(buffer);

        // Before targeting a new buffer we attempt to snapshot the previous one!
        trySnapshot();
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
