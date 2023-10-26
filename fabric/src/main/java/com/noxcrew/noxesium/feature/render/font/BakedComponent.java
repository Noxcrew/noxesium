package com.noxcrew.noxesium.feature.render.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

/**
 * A pre-baked component that can be immediately rendered in the known style.
 */
public class BakedComponent {

    public final GuiGraphicsExt.StringRenderOutput renderOutput;
    public final boolean hasObfuscation;

    public BakedComponent(Component component) {
        this(component, Minecraft.getInstance().font);
    }

    public BakedComponent(Component component, Font font) {
        renderOutput = new GuiGraphicsExt.StringRenderOutput(font);
        component.getVisualOrderText().accept(renderOutput);
        renderOutput.clean();
        this.hasObfuscation = renderOutput.doesContainObfuscation();
    }
}
