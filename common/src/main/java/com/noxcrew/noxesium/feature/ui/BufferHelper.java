package com.noxcrew.noxesium.feature.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.noxcrew.noxesium.feature.ui.render.DynamicElement;
import com.noxcrew.noxesium.feature.ui.render.SharedVertexBuffer;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.opengl.GL14;

/**
 * Helps in binding general properties before starting rendering of any buffer.
 */
public class BufferHelper {

    private static boolean isBound;

    /**
     * Prepared for any frame buffer to be bound.
     */
    public static void bind(GuiGraphics guiGraphics) {
        if (isBound) return;
        isBound = true;

        // Flush the gui graphics to finish drawing to whatever it was on
        guiGraphics.flush();

        // Prepare a correct constant blending color
        GL14.glBlendColor(1f, 1f, 1f, 1f);

        // Pre-enable the blending state
        DynamicElement.DEFAULT_BLEND_STATE.apply();
    }

    /**
     * Unbinds any frame buffer if one was bound.
     */
    public static void unbind() {
        if (!isBound) return;
        isBound = false;

        // Reset the blending state
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();

        // Rebind the main render target
        SharedVertexBuffer.rebindMainRenderTarget();
    }
}
