package com.noxcrew.noxesium.feature.ui.render.api;

import com.noxcrew.noxesium.NoxesiumMod;
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
        // Ignore checks if we're not using dynamic UI limiting!
        // However, if we are using dynamic UI limiting we want to make
        // sure we always draw on frames after a client tick happened as
        // it has the newest frame data.
        if (!NoxesiumMod.getInstance().getConfig().shouldUseDynamicUiLimiting()) return;

        var state = get();
        if (state != null) {
            state.requestCheck();
        }
    }
}
