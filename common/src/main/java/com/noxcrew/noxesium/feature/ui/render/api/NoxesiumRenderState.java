package com.noxcrew.noxesium.feature.ui.render.api;

import com.noxcrew.noxesium.feature.ui.render.DynamicElement;
import java.io.Closeable;
import java.util.List;

/**
 * The basis for a render state object.
 */
public abstract class NoxesiumRenderState implements Closeable {

    /**
     * The amount of screen renders that occurred in the last second.
     */
    public final PerSecondTrackedValue renders = new PerSecondTrackedValue();

    /**
     * Returns all dynamic elements in this state.
     */
    public abstract List<DynamicElement> getDynamics();

    /**
     * Ticks this render state.
     */
    public void tick() {
        for (var dynamic : getDynamics()) {
            dynamic.tick();
        }
    }

    /**
     * Indicates that a check should run the very next frame.
     */
    public void requestCheck() {
        for (var dynamic : getDynamics()) {
            dynamic.redraw();
        }
    }

    /**
     * Triggers an update of the render framerate.
     */
    public void updateRenderFramerate() {
        for (var dynamic : getDynamics()) {
            dynamic.resetToMax();
        }
    }
}
