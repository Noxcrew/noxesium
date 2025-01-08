package com.noxcrew.noxesium.feature.ui.render.api;

import java.io.Closeable;

/**
 * The basis for a render state object.
 */
public abstract class NoxesiumRenderState implements Closeable {

    /**
     * The amount of screen renders that occurred in the last second.
     */
    public final PerSecondTrackedValue renders = new PerSecondTrackedValue();

    /**
     * Ticks this render state.
     */
    public abstract void tick();

    /**
     * Indicates that a check should run the very next frame.
     */
    public abstract void requestCheck();

    /**
     * Triggers an update of the render framerate.
     */
    public abstract void updateRenderFramerate();
}
