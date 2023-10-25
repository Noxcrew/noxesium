package com.noxcrew.noxesium.feature.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;

/**
 * Provides a function to render the scoreboard efficiently.
 */
public class ScoreboardRenderer {

    /**
     * Renders the scoreboard to the screen using the cached contents. We optimize scoreboard rendering in multiple levels:
     * - Render background in one call instead of 15
     * - Cache contents of the scoreboard
     * - Cache structure of text in scoreboard
     * - Micro-optimizations to rendering code itself
     * - Drawing into an intermediate buffer
     */
    public static void renderScoreboard(GuiGraphics graphics, int screenWidth, int screenHeight, Minecraft minecraft) {
        var cache = CachedScoreboardContents.getCachedScoreboardContents();
        var font = Minecraft.getInstance().font;

        var height = cache.lines().size() * 9;
        var bottom = screenHeight / 2 + height / 3;
        var right = 3;
        var left = screenWidth - cache.maxWidth() - right;

        // Draw the buffered contents of the scoreboard to the screen as a base!
        var screenBuffer = CachedScoreboardContents.getScoreboardBuffer();
        screenBuffer.draw();

        if (!cache.hasObfuscation()) return;

        // We always draw the lines that have obfuscation overtop!
        graphics.drawManaged(() -> {
            // Draw the header if it has obfuscation
            var headerTop = bottom - cache.lines().size() * 9;
            if (cache.header().hasObfuscation) {
                GuiGraphicsExt.drawString(graphics, font, cache.header(), left + cache.maxWidth() / 2 - cache.headerWidth() / 2, headerTop - 9, -1, false);
            }

            // Line 1 here is the bottom line, this is because the
            // finalScores are sorted and we're getting them still
            // ordered ascending, so we want to display the last
            // score at the top.
            for (var line = 1; line <= cache.lines().size(); line++) {
                var text = cache.lines().get(line - 1);
                if (!text.hasObfuscation) continue;

                var lineTop = bottom - line * 9;
                GuiGraphicsExt.drawString(graphics, font, text, left, lineTop, -1, false);
            }
        });
    }
}
