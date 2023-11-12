package com.noxcrew.noxesium.feature.render.cache.actionbar;

import com.noxcrew.noxesium.feature.render.cache.ElementCache;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * Manages the current cache of the action bar.
 */
public class ActionBarCache extends ElementCache<ActionBarInformation> {

    private static ActionBarCache instance;

    public static float lastPartialTicks = 0f;
    public static GuiGraphics graphics = null;

    /**
     * Returns the current instance of this action bar cache.
     */
    public static ActionBarCache getInstance() {
        if (instance == null) {
            instance = new ActionBarCache();
        }
        return instance;
    }

    @Override
    protected ActionBarInformation createCache(Minecraft minecraft, Font font) {
        var gui = minecraft.gui;
        if (gui.overlayMessageString == null || gui.overlayMessageTime <= 0) {
            return ActionBarInformation.EMPTY;
        }
        var baked = new BakedComponent(gui.overlayMessageString, font);
        return new ActionBarInformation(baked);
    }

    @Override
    protected void render(GuiGraphics graphics, ActionBarInformation cache, Minecraft minecraft, int screenWidth, int screenHeight, Font font, boolean buffered) {
        var gui = minecraft.gui;
        if (gui.overlayMessageString == null || gui.overlayMessageTime <= 0) return;
        var remainingTicks = (float) gui.overlayMessageTime - lastPartialTicks;

        // Determine the transparency of the text, if above 1s is left
        // it's fixed at maximum.
        var fixed = false;
        var alpha = (int) (remainingTicks * 255.0F / 20.0F);
        if (alpha > 255) {
            alpha = 255;
            fixed = true;
        }

        // Animated text can never be fixed!
        if (gui.animateOverlayMessageColor || cache.component().needsCustomReRendering) {
            fixed = false;
        }

        // Only render in this layer if the text is fixed or not
        if (fixed != buffered) return;

        // If at least a transparency of 8 is left
        if (alpha > 8) {
            var pose = graphics.pose();
            pose.pushPose();
            pose.translate((float) (screenWidth / 2), (float) (screenHeight - 68), 0.0F);

            // If the text is being animated we alter the color (used by jukeboxes)
            var textColor = 16777215;
            if (gui.animateOverlayMessageColor) {
                textColor = Mth.hsvToRgb(remainingTicks / 50.0F, 0.7F, 0.6F) & 16777215;
            }

            var trueAlpha = alpha << 24 & -16777216;
            var width = cache.component().width;
            var background = minecraft.options.getBackgroundColor(0.0F);
            var color = 16777215 | trueAlpha;
            var offset = -4;

            if (background != 0) {
                int j = -width / 2;
                graphics.fill(j - 2, offset - 2, j + width + 2, offset + 9 + 2, FastColor.ARGB32.multiply(background, color));
            }
            cache.component().draw(graphics, font, -width / 2, -4, textColor | trueAlpha);
            pose.popPose();
        }
    }
}
