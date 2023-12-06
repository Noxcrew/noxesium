package com.noxcrew.noxesium.feature.render.cache.actionbar;

import com.noxcrew.noxesium.feature.render.cache.ElementCache;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import com.noxcrew.noxesium.mixin.performance.render.ext.GuiExt;
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
            var guiExt = (GuiExt) gui;
            var remainingTicks = (float) guiExt.getOverlayMessageTime() - partialTicks;
            var alpha = (int) (remainingTicks * 255.0F / 20.0F);
            return Mth.clamp(alpha, 0, 255);
        });
    }

    @Override
    protected ActionBarInformation createCache(Minecraft minecraft, Font font) {
        var gui = minecraft.gui;
        var guiExt = (GuiExt) gui;
        if (guiExt.getOverlayMessageString() == null || guiExt.getOverlayMessageTime() <= 0) {
            return ActionBarInformation.EMPTY;
        }
        var baked = new BakedComponent(guiExt.getOverlayMessageString(), font);
        return new ActionBarInformation(baked, getVariable("alpha"));
    }

    @Override
    protected void render(GuiGraphics graphics, ActionBarInformation cache, Minecraft minecraft, int screenWidth, int screenHeight, Font font, float partialTicks, boolean dynamic) {
        var gui = minecraft.gui;
        var guiExt = (GuiExt) gui;
        var remainingTicks = (float) guiExt.getOverlayMessageTime() - partialTicks;

        // If at least a transparency of 8 is left
        var alpha = cache.alpha();
        if (alpha > 8) {
            var pose = graphics.pose();
            pose.pushPose();
            pose.translate((float) (screenWidth / 2), (float) (screenHeight - 68), 0.0F);

            // If the text is being animated we alter the color (used by jukeboxes)
            var textColor = 16777215;
            if (guiExt.getAnimateOverlayMessageColor()) {
                textColor = Mth.hsvToRgb(remainingTicks / 50.0F, 0.7F, 0.6F) & 16777215;

                // Don't draw to anything but the dynamic layer if the text is changing!
                if (!dynamic) return;
            }

            var trueAlpha = alpha << 24 & -16777216;
            var width = cache.component().width;
            var background = minecraft.options.getBackgroundColor(0.0F);
            var backgroundColor = FastColor.ARGB32.multiply(background, 16777215 | trueAlpha);
            var offset = -4;

            if (!dynamic && background != 0) {
                var left = -width / 2;
                graphics.fill(left - 2, offset - 2, left + width + 2, offset + 9 + 2, backgroundColor);
            }
            if (guiExt.getAnimateOverlayMessageColor() || cache.component().shouldDraw(dynamic)) {
                cache.component().draw(graphics, font, -width / 2, -4, textColor | trueAlpha);
            }
            pose.popPose();
        }
    }
}
