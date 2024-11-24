package com.noxcrew.noxesium.feature.ui.layer;

import net.minecraft.client.gui.LayeredDraw;

/**
 * Extends layered draw by adding Noxesium's custom wrapper object.
 */
public interface LayeredDrawExtension {

    /**
     * Returns the Noxesium wrapper object around this layer.
     */
    NoxesiumLayeredDraw noxesium$get();

    /**
     * Adds a new layer with a given name.
     */
    void noxesium$addLayer(String name, LayeredDraw.Layer layer);
}
