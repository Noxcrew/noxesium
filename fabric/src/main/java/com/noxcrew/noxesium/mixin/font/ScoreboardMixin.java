package com.noxcrew.noxesium.mixin.font;

import com.mojang.datafixers.util.Pair;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(Gui.class)
public abstract class ScoreboardMixin {

    @Shadow public abstract Font getFont();

    @Shadow private int screenHeight;

    @Shadow private int screenWidth;

    @Shadow @Final private Minecraft minecraft;

    /**
     * @author Aeltumn
     * @reason Use unmanaged mode on graphics to render entire scoreboard in one draw call.
     */
    @Overwrite
    public void displayScoreboardSidebar(GuiGraphics graphics, Objective objective) {
        var drawNumbers = !ServerRules.DISABLE_SCOREBOARD_NUMBER_RENDERING.getValue();

        var scoreboard = objective.getScoreboard();
        var scores = (List<Score>) scoreboard.getPlayerScores(objective);
        var finalScores = new ArrayList<Pair<Score, Component>>();
        var numberWidths = new ArrayList<Integer>();

        var title = objective.getDisplayName();
        var lowerLimit = scores.size() - 15;
        var headerWidth = getFont().width(title);
        var maxWidth = headerWidth;
        var extraWidth = getFont().width(": ");

        for (var index = scores.size() - 1; index >= lowerLimit && index >= 0; index--) {
            var score = scores.get(index);
            if (score.getOwner() == null || score.getOwner().startsWith("#")) {
                // If we skip a line we can continue iterating further down
                lowerLimit--;
                continue;
            }

            // We are iterating in reverse order, so we add at the start of the list.
            // We want to end up with the last 15 entries of scores.
            var playerteam = scoreboard.getPlayersTeam(score.getOwner());
            var text = PlayerTeam.formatNameForTeam(playerteam, Component.literal(score.getOwner()));
            finalScores.add(0, Pair.of(score, text));

            // Update the maximum width we've found, we always include the numbers here even if we're requested
            // to not render them as we assume the server is already doing some moving of the background to get
            // things to line up. We don't want to mess with that, just save the rendering time.
            var number = "" + ChatFormatting.RED + score.getScore();
            var numberWidth = getFont().width(number);
            maxWidth = Math.max(maxWidth, getFont().width(text) + extraWidth + numberWidth);
            numberWidths.add(0, numberWidth);
        }

        var height = finalScores.size() * 9;
        var bottom = this.screenHeight / 2 + height / 3;
        var right = 3;
        var left = this.screenWidth - maxWidth - right;
        var backgroundRight = this.screenWidth - right + 2;
        var background = minecraft.options.getBackgroundColor(0.3f);
        var darkerBackground = minecraft.options.getBackgroundColor(0.4f);

        final var finalMaxWidth = maxWidth;
        graphics.drawManaged(() -> {
            // Draw the header
            var headerTop = bottom - finalScores.size() * 9;
            graphics.fill(left - 2, headerTop - 9 - 1, backgroundRight, headerTop - 1, darkerBackground);
            graphics.drawString(getFont(), title, left + finalMaxWidth / 2 - headerWidth / 2, headerTop - 9, -1, false);

            // Draw the background (vanilla does this per line but we do it once)
            graphics.fill(left - 2, bottom, backgroundRight, headerTop - 1, background);

            // Line 1 here is the bottom line, this is because the
            // finalScores are sorted and we're getting them still
            // ordered ascending, so we want to display the last
            // score at the top.
            for (var line = 1; line <= finalScores.size(); line++) {
                var pair = finalScores.get(line - 1);
                var score = pair.getFirst();
                var text = pair.getSecond();
                var lineTop = bottom - line * 9;
                graphics.drawString(getFont(), text, left, lineTop, -1, false);

                if (drawNumbers) {
                    var number = "" + ChatFormatting.RED + score.getScore();
                    graphics.drawString(getFont(), number, backgroundRight - numberWidths.get(line - 1), lineTop, -1, false);
                }
            }
        });
    }
}
