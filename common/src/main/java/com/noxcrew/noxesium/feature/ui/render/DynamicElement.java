package com.noxcrew.noxesium.feature.ui.render;

import com.noxcrew.noxesium.NoxesiumMod;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Manages a buffer and its current dynamic fps.
 */
public class DynamicElement extends Element {

    private final List<SnapshotElementBuffer> buffers = new ArrayList<>();

    private boolean canCheck = true;

    private long nextRender = -1;
    private long nextCheck = System.nanoTime() + 1000000000;
    private int lastFps = 0;

    private boolean movementDirection = false;
    private long lastChange = System.currentTimeMillis();
    private int matches = 0;

    /**
     * The current fps at which we re-render the UI elements.
     */
    private double renderFps = NoxesiumMod.getInstance().getConfig().maxUiFramerate;

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
     * The current frame rate of this group.
     */
    public int renderFramerate() {
        return (int) Math.floor(renderFps);
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
     * Returns the true frame rate, the amount
     * of times this element was rendered the last
     * second.
     */
    public int framerate() {
        return lastFps;
    }

    /**
     * Returns whether this element is ready to be considered
     * for group merging/joining.
     */
    public boolean isReady() {
        if (needsRedraw()) return false;
        if (isNotEmpty()) {
            for (var buffer : buffers) {
                if (buffer instanceof SnapshotElementBuffer pboBuffer && !pboBuffer.hasValidPBO()) {
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
        var verdict = !hasChangedLayers;
        if (isNotEmpty()) {
            for (var buffer : buffers) {
                if (buffer instanceof SnapshotElementBuffer pboBuffer) {
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
            renderFps = Math.clamp(
                    Math.max(
                            max * (1.0 - ((double) (System.currentTimeMillis() - lastChange) / 10000d)),
                            max * ((double) (60 - matches) / 10d)),
                    0,
                    max);

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
                if (buffer instanceof SnapshotElementBuffer pboBuffer) {
                    pboBuffer.requestNewPBO();
                }
            }
        }
    }

    /** Tries to make a snapshot of the current buffer. */
    private void trySnapshot() {
        if (elementsWereDrawn && canCheck) {
            var target = getTargetBuffer();
            if (target instanceof SnapshotElementBuffer pboBuffer && pboBuffer.canSnapshot()) {
                pboBuffer.snapshot();
            }
        }
    }

    @Override
    public boolean update(long nanoTime, GuiGraphics guiGraphics, Runnable draw) {
        // Always start by awaiting the GPU fence
        for (var buffer : buffers) {
            if (buffer instanceof SnapshotElementBuffer pboBuffer) {
                pboBuffer.awaitFence();
            }
        }

        // Initialize the value if it's missing
        if (nextRender == -1) {
            nextRender = nanoTime;
        }

        // Skip the update until we reach the next render time
        if (!needsRedraw && !canCheck && (renderFps <= 20 || nextRender > nanoTime)) return false;
        needsRedraw = false;

        // Set the next render time
        nextRender = nanoTime + (long) Math.floor(((1 / renderFps) * 1000000000));

        var result = super.update(nanoTime, guiGraphics, draw);
        if (result) {
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
    public ElementBuffer createBuffer() {
        return new SnapshotElementBuffer();
    }
}
