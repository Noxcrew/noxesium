package com.noxcrew.noxesium.feature.ui.render.api;

import java.io.Closeable;

/**
 * The basis for a render state object.
 */
public interface NoxesiumRenderState extends Closeable {

    /**
     * Ticks this render state.
     */
    void tick();
}
