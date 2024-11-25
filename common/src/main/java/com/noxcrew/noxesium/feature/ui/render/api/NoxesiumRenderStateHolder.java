package com.noxcrew.noxesium.feature.ui.render.api;

import org.jetbrains.annotations.Nullable;

/**
 * An object that holds a render state.
 */
public interface NoxesiumRenderStateHolder<T extends NoxesiumRenderState> {

    /**
     * Returns the current render state.
     */
    @Nullable
    NoxesiumRenderState get();

    /**
     * Clears the current state.
     */
    void clear();

    /**
     * Triggers an update of the render framerate.
     */
    default void updateRenderFramerate() {
        var state = get();
        if (state != null) {
            state.updateRenderFramerate();
        }
    }

    /**
     * Indicates that a check should run the very next frame.
     */
    default void requestCheck() {
        var state = get();
        if (state != null) {
            state.requestCheck();
        }
    }
}
