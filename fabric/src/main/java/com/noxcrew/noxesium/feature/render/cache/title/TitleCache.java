package com.noxcrew.noxesium.feature.render.cache.title;

import com.mojang.blaze3d.systems.RenderSystem;
import com.noxcrew.noxesium.feature.render.cache.ElementCache;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * Manages the current cache of the title.
 */
public class TitleCache extends ElementCache<TitleInformation> {

    private static TitleCache instance;

    /**
     * Returns the current instance of this title cache.
     */
    public static TitleCache getInstance() {
        if (instance == null) {
            instance = new TitleCache();
        }
        return instance;
    }

    /**
     * Returns the current alpha of the text.
     */
    protected int getAlpha(Gui gui, float partialTicks) {
        var alpha = 255;
        var ticksElapsed = (float) gui.titleTime - partialTicks;

        if (gui.titleTime > gui.titleFadeOutTime + gui.titleStayTime) {
            float f6 = (float) (gui.titleFadeInTime + gui.titleStayTime + gui.titleFadeOutTime) - ticksElapsed;
            alpha = (int) (f6 * 255.0F / (float) gui.titleFadeInTime);
        }

        if (gui.titleTime <= gui.titleFadeOutTime) {
            alpha = (int) (ticksElapsed * 255.0F / (float) gui.titleFadeOutTime);
        }

        return Mth.clamp(alpha, 0, 255);
    }

    @Override
    protected boolean isEmpty(TitleInformation cache) {
        return cache == TitleInformation.EMPTY;
    }

    @Override
    protected TitleInformation createCache(Minecraft minecraft, Font font) {
        var gui = minecraft.gui;
        if (gui.title == null || gui.titleTime <= 0) {
            return TitleInformation.EMPTY;
        }

        return new TitleInformation(
                new BakedComponent(gui.title, font),
                gui.subtitle == null ? null : new BakedComponent(gui.subtitle, font),
                getAlpha(gui, 0f) != 255
        );
    }

    @Override
    protected void render(GuiGraphics graphics, TitleInformation cache, Minecraft minecraft, int screenWidth, int screenHeight, Font font, float partialTicks, boolean buffered) {
        // If the title is null we should clear the cache and return because it faded out!
        // (this detects the title changes from the tick() method)
        var gui = minecraft.gui;
        if (gui.titleTime <= 0) {
            clearCache();
            return;
        }

        // Determine the transparency of the text and determine if it's at the maximum
        var alpha = getAlpha(gui, partialTicks);
        var fading = cache.fading();
        var shouldBeFading = alpha != 255;

        // Check if the fading has started and the buffer doesn't match,
        // we can only clear the cache for next tick so we still use
        // the cached fading state for the logic.
        if (!buffered && shouldBeFading != fading) {
            clearCache();
        }

        // Only render in this layer if the text is fixed or not
        if (fading == buffered) return;

        if (alpha > 8) {
            graphics.pose().pushPose();
            graphics.pose().translate((float) (screenWidth / 2), (float) (screenHeight / 2), 0.0F);
            RenderSystem.enableBlend();
            graphics.pose().pushPose();
            graphics.pose().scale(4.0F, 4.0F, 4.0F);

            var trueAlpha = alpha << 24 & 0xFF000000;
            var background = minecraft.options.getBackgroundColor(0.0F);
            var backgroundColor = FastColor.ARGB32.multiply(background, 16777215 | trueAlpha);

            var titleWidth = cache.title().width;
            if (buffered) {
                var offset = -10;
                if (background != 0) {
                    var left = -titleWidth / 2;
                    graphics.fill(left - 2, offset - 2, left + titleWidth + 2, offset + 9 + 2, backgroundColor);
                }
            }
            if (cache.title().shouldDraw(buffered)) {
                cache.title().draw(graphics, font, -titleWidth / 2, -10, 16777215 | trueAlpha);
            }
            graphics.pose().popPose();
            if (cache.subtitle() != null) {
                graphics.pose().pushPose();
                graphics.pose().scale(2.0F, 2.0F, 2.0F);
                var subtitleWidth = cache.subtitle().width;
                if (buffered) {
                    var offset = 5;
                    if (background != 0) {
                        var left = -subtitleWidth / 2;
                        graphics.fill(left - 2, offset - 2, left + subtitleWidth + 2, offset + 9 + 2, backgroundColor);
                    }
                }
                if (cache.subtitle().shouldDraw(buffered)) {
                    cache.subtitle().draw(graphics, font, -subtitleWidth / 2, 5, 16777215 | trueAlpha);
                }
                graphics.pose().popPose();
            }

            RenderSystem.disableBlend();
            graphics.pose().popPose();
        }
    }
}
