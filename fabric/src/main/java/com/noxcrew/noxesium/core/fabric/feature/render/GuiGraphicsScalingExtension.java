package com.noxcrew.noxesium.core.fabric.feature.render;

import com.noxcrew.noxesium.api.client.GuiElement;

/**
 * Extends GuiGraphics to allow temporarily rescaling an element.
 */
public interface GuiGraphicsScalingExtension {
    /**
     * Runs the given function while rescaled for the given element.
     */
    public void noxesium$whileRescaled(GuiElement element, Runnable function);
}
