package com.noxcrew.noxesium.feature.render.cache.actionbar;

import com.noxcrew.noxesium.feature.render.cache.ElementCache;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * Manages the current cache of the action bar.
 */
public class ActionBarCache extends ElementCache<ActionBarInformation> {

    private static ActionBarCache instance;

    /**
     * Returns the current instance of this action bar cache.
     */
    public static ActionBarCache getInstance() {
        if (instance == null) {
            instance = new ActionBarCache();
        }
        return instance;
    }

    public ActionBarCache() {
        registerVariable("alpha", (minecraft, partialTicks) -> {
            var gui = minecraft.gui;
            var remainingTicks = (float) gui.overlayMessageTime - partialTicks;
            var alpha = (int) (remainingTicks * 255.0F / 20.0F);
            return Mth.clamp(alpha, 0, 255);
        });
    }

    @Override
    protected boolean isEmpty(ActionBarInformation cache) {
        return cache == ActionBarInformation.EMPTY;
    }

    @Override
    protected ActionBarInformation createCache(Minecraft minecraft, Font font) {
        var gui = minecraft.gui;
        if (gui.overlayMessageString == null || gui.overlayMessageTime <= 0) {
            return ActionBarInformation.EMPTY;
        }
        var baked = new BakedComponent(gui.overlayMessageString, font);
        return new ActionBarInformation(baked, getVariable("alpha"));
    }

    @Override
    protected void render(GuiGraphics graphics, ActionBarInformation cache, Minecraft minecraft, int screenWidth, int screenHeight, Font font, float partialTicks, boolean buffered) {
        var gui = minecraft.gui;
        var remainingTicks = (float) gui.overlayMessageTime - partialTicks;

        // If at least a transparency of 8 is left
        var alpha = cache.alpha();
        if (alpha > 8) {
            var pose = graphics.pose();
            pose.pushPose();
            pose.translate((float) (screenWidth / 2), (float) (screenHeight - 68), 0.0F);

            // If the text is being animated we alter the color (used by jukeboxes)
            var textColor = 16777215;
            if (gui.animateOverlayMessageColor) {
                textColor = Mth.hsvToRgb(remainingTicks / 50.0F, 0.7F, 0.6F) & 16777215;

                // Don't draw if changing color and in buffer!
                if (buffered) return;
            }

            var trueAlpha = alpha << 24 & -16777216;
            var width = cache.component().width;
            var background = minecraft.options.getBackgroundColor(0.0F);
            var backgroundColor = FastColor.ARGB32.multiply(background, 16777215 | trueAlpha);
            var offset = -4;

            if (buffered && background != 0) {
                var left = -width / 2;
                graphics.fill(left - 2, offset - 2, left + width + 2, offset + 9 + 2, backgroundColor);
            }
            if (gui.animateOverlayMessageColor || cache.component().shouldDraw(buffered)) {
                cache.component().draw(graphics, font, -width / 2, -4, textColor | trueAlpha);
            }
            pose.popPose();
        }
    }
}
