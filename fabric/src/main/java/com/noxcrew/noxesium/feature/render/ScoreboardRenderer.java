package com.noxcrew.noxesium.feature.render;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Provides a function to render the scoreboard efficiently.
 */
public class ScoreboardRenderer {

    /**
     * Renders the scoreboard to the screen using the cached contents.
     */
    public static void renderScoreboard(GuiGraphics graphics, int screenWidth, int screenHeight, Minecraft minecraft) {
        var cache = CachedScoreboardContents.getCachedScoreboardContents();
        var drawNumbers = cache.drawNumbers();
        var font = Minecraft.getInstance().font;

        var height = cache.lines().size() * 9;
        var bottom = screenHeight / 2 + height / 3;
        var right = 3;
        var left = screenWidth - cache.maxWidth() - right;
        var backgroundRight = screenWidth - right + 2;
        var background = minecraft.options.getBackgroundColor(0.3f);
        var darkerBackground = minecraft.options.getBackgroundColor(0.4f);

        // Draw the header
        var headerTop = bottom - cache.lines().size() * 9;
        graphics.fill(left - 2, headerTop - 9 - 1, backgroundRight, headerTop - 1, darkerBackground);
        graphics.drawString(font, cache.header(), left + cache.maxWidth() / 2 - cache.headerWidth() / 2, headerTop - 9, -1, false);

        // Draw the background (vanilla does this per line but we do it once)
        graphics.fill(left - 2, bottom, backgroundRight, headerTop - 1, background);

        // Line 1 here is the bottom line, this is because the
        // finalScores are sorted and we're getting them still
        // ordered ascending, so we want to display the last
        // score at the top.
        for (var line = 1; line <= cache.lines().size(); line++) {
            var pair = cache.lines().get(line - 1);
            var score = pair.getFirst();
            var text = pair.getSecond();
            var lineTop = bottom - line * 9;
            graphics.drawString(font, text, left, lineTop, -1, false);

            if (drawNumbers) {
                var number = String.valueOf(ChatFormatting.RED) + score.getScore();
                graphics.drawString(font, number, backgroundRight - cache.numberWidths().get(line - 1), lineTop, -1, false);
            }
        }
    }
}
