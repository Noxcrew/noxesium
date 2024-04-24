package com.noxcrew.noxesium.feature.render.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * A baked component builder which adds helper functions for configuring the
 * way in which a baked component is configured.
 */
public class BakedComponentBuilder {

    private final FormattedCharSequence component;
    private final Font font;

    /**
     * If `true` a second instance of the font is drawn slightly down
     * and behind the original text in a darker color.
     */
    public boolean shadow = true;

    public BakedComponentBuilder(Component component) {
        this(component, Minecraft.getInstance().font);
    }

    public BakedComponentBuilder(Component component, Font font) {
        this(component.getVisualOrderText(), font);
    }

    public BakedComponentBuilder(FormattedCharSequence component, Font font) {
        this.component = component;
        this.font = font;
    }

    /**
     * Creates the baked component.
     */
    public BakedComponent build() {
        return new BakedComponent(component, font, shadow);
    }
}
