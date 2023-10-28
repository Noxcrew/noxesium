package com.noxcrew.noxesium.feature.render.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * A pre-baked component that can be immediately rendered in the known style.
 */
public class BakedComponent {

    /**
     * An empty baked component.
     */
    public static final BakedComponent EMPTY = new BakedComponent(Component.empty());
    
    public final GuiGraphicsExt.StringRenderOutput renderOutput;
    public final boolean hasObfuscation;
    public final int width;

    public BakedComponent(Component component) {
        this(component, Minecraft.getInstance().font);
    }

    public BakedComponent(Component component, Font font) {
        this(component.getVisualOrderText(), font);
    }

    public BakedComponent(FormattedCharSequence component, Font font) {
        renderOutput = new GuiGraphicsExt.StringRenderOutput(font);
        component.accept(renderOutput);
        this.hasObfuscation = renderOutput.doesContainObfuscation();
        this.width = font.width(component);
    }
}
