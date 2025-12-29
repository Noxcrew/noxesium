package com.noxcrew.noxesium.core.fabric.feature;

import com.noxcrew.noxesium.core.feature.GuiElement;

/**
 * Extends a base class to allow temporarily rescaling an element.
 */
public interface ScalingExtension {
    /**
     * Runs the given function while rescaled for the given element.
     */
    public void noxesium$whileRescaled(GuiElement element, Runnable function);
}
