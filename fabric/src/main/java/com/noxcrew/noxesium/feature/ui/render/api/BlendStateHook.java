package com.noxcrew.noxesium.feature.ui.render.api;

/**
 * A hook that receives any changes to the blending state.
 */
public interface BlendStateHook {

    /**
     * Called when the blending state is attempted to
     * be changed to [newValue]. If `true` is returned
     * the call is prevented.
     */
    boolean changeState(boolean newValue);

    /**
     * Called when the blending function is attempted to
     * be changed to the new values. If `true` is returned
     * the call is prevented.
     */
    boolean changeFunc(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha);
}
