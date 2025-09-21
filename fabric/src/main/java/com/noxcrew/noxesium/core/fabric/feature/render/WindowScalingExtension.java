package com.noxcrew.noxesium.core.fabric.feature.render;

import com.noxcrew.noxesium.api.client.GuiElement;

/**
 * Extends Window to allow temporarily rescaling GUI elements.
 */
public interface WindowScalingExtension {
    /**
     * Runs the given function while rescaled for the given element.
     */
    public void noxesium$whileRescaled(GuiElement element, Runnable function);
}
