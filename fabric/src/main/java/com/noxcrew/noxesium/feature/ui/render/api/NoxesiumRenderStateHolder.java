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
}
