package com.noxcrew.noxesium.feature.render.cache;

import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import com.noxcrew.noxesium.feature.render.font.GuiGraphicsExt;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the current cache of the scoreboard.
 */
public class ScoreboardCache extends ElementCache<ScoreboardInformation> {

    private static ScoreboardCache instance;

    /**
     * Returns the current instance of this scoreboard cache.
     */
    public static ScoreboardCache getInstance() {
        if (instance == null) {
            instance = new ScoreboardCache();
        }
        return instance;
    }

    /**
     * Returns whether the given player is relevant to the current cache.
     */
    public boolean isPlayerRelevant(String player) {
        if (cache == null) return false;
        return cache.players().contains(player);
    }

    /**
     * Returns whether the given team is relevant to the current cache.
     */
    public boolean isTeamRelevant(String team) {
        if (cache == null) return false;
        return cache.teams().contains(team);
    }

    /**
     * Returns whether the given objective is relevant to the current cache.
     * We compare against the exact instance of the objective for speed.
     */
    public boolean isObjectiveRelevant(Objective objective) {
        if (cache == null || cache.objective() == null) return false;
        return cache.objective() == objective;
    }

    /**
     * Creates newly cached scoreboard content information.
     * <p>
     * Depends on the following information:
     * - Current resource pack configuration
     * - Current scoreboard contents of objective
     * - The draw numbers setting
     * - Any visual properties of the associated teams (team of client player and teams of all players in objective)
     */
    @Override
    public ScoreboardInformation createCache() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return ScoreboardInformation.EMPTY;
        }

        // Determine the currently shown objective
        var scoreboard = player.getScoreboard();
        Objective objective = null;
        var localPlayerTeam = scoreboard.getPlayersTeam(player.getScoreboardName());
        if (localPlayerTeam != null) {
            var displaySlot = DisplaySlot.teamColorToSlot(localPlayerTeam.getColor());
            if (displaySlot != null) {
                objective = scoreboard.getDisplayObjective(displaySlot);
            }
        }
        if (objective == null) {
            objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        }
        if (objective == null) {
            return ScoreboardInformation.EMPTY;
        }

        var drawNumbers = !ServerRules.DISABLE_SCOREBOARD_NUMBER_RENDERING.getValue();
        var font = Minecraft.getInstance().font;
        var hasObfuscation = false;

        var players = new ArrayList<String>();
        var teams = new ArrayList<String>();
        players.add(player.getScoreboardName());
        if (localPlayerTeam != null) {
            teams.add(localPlayerTeam.getName());
        }

        var scores = (List<Score>) scoreboard.getPlayerScores(objective);
        var finalScores = new ArrayList<BakedComponent>();
        var numberWidths = new ArrayList<Integer>();
        var numbers = new ArrayList<BakedComponent>();

        var title = objective.getDisplayName();
        var lowerLimit = scores.size() - 15;
        var headerWidth = font.width(title);
        var maxWidth = headerWidth;
        var extraWidth = drawNumbers ? font.width(": ") : 0;

        for (var index = scores.size() - 1; index >= lowerLimit && index >= 0; index--) {
            var score = scores.get(index);
            if (score.getOwner() == null || score.getOwner().startsWith("#")) {
                // If we skip a line we can continue iterating further down
                lowerLimit--;
                continue;
            }

            // We are iterating in reverse order, so we add at the start of the list.
            // We want to end up with the last 15 entries of scores.
            var playerTeam = scoreboard.getPlayersTeam(score.getOwner());
            players.add(score.getOwner());
            teams.add(playerTeam.getName());
            var text = PlayerTeam.formatNameForTeam(playerTeam, Component.literal(score.getOwner()));
            var baked = new BakedComponent(text);
            finalScores.add(0, baked);
            if (baked.hasObfuscation) hasObfuscation = true;

            // Update the maximum width we've found, if numbers are being omitted we move the whole
            // background right.
            var number = "" + ChatFormatting.RED + score.getScore();
            var numberWidth = drawNumbers ? font.width(number) : 0;
            maxWidth = Math.max(maxWidth, font.width(text) + extraWidth + numberWidth);
            if (drawNumbers) {
                numberWidths.add(0, numberWidth);
                numbers.add(0, new BakedComponent(Component.literal(String.valueOf(score.getScore())).withStyle(ChatFormatting.RED)));
            }
        }

        var header = new BakedComponent(title);
        return new ScoreboardInformation(
                objective,
                players,
                teams,
                header,
                finalScores,
                numbers,
                numberWidths,
                headerWidth,
                maxWidth,
                header.hasObfuscation || hasObfuscation
        );
    }

    /**
     * Renders the scoreboard to the screen using the cached contents. We optimize scoreboard rendering in multiple levels:
     * - Render background in one call instead of 15
     * - Cache contents of the scoreboard
     * - Cache structure of text in scoreboard
     * - Micro-optimizations to rendering code itself
     * - Drawing into an intermediate buffer
     */
    @Override
    public void renderDirect(GuiGraphics graphics, ScoreboardInformation cache, int screenWidth, int screenHeight, Minecraft minecraft) {
        super.renderDirect(graphics, cache, screenWidth, screenHeight, minecraft);
        if (!cache.hasObfuscation()) return;

        var font = minecraft.font;
        var height = cache.lines().size() * 9;
        var bottom = screenHeight / 2 + height / 3;
        var right = 3;
        var left = screenWidth - cache.maxWidth() - right;

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

    @Override
    public void renderBuffered(GuiGraphics graphics, ScoreboardInformation cache, Minecraft minecraft, int screenWidth, int screenHeight, Font font) {
        var height = cache.lines().size() * 9;
        var bottom = screenHeight / 2 + height / 3;
        var right = 3;
        var left = screenWidth - cache.maxWidth() - right;

        var backgroundRight = screenWidth - right + 2;
        var background = Minecraft.getInstance().options.getBackgroundColor(0.3f);
        var darkerBackground = Minecraft.getInstance().options.getBackgroundColor(0.4f);

        graphics.drawManaged(() -> {
            // Draw the header
            var headerTop = bottom - cache.lines().size() * 9;
            graphics.fill(left - 2, headerTop - 9 - 1, backgroundRight, headerTop - 1, darkerBackground);
            if (!cache.header().hasObfuscation) {
                GuiGraphicsExt.drawString(graphics, font, cache.header(), left + cache.maxWidth() / 2 - cache.headerWidth() / 2, headerTop - 9, -1, false);
            }

            // Draw the background (vanilla does this per line but we do it once)
            graphics.fill(left - 2, bottom, backgroundRight, headerTop - 1, background);

            // Line 1 here is the bottom line, this is because the
            // finalScores are sorted and we're getting them still
            // ordered ascending, so we want to display the last
            // score at the top.
            for (var line = 1; line <= cache.lines().size(); line++) {
                var text = cache.lines().get(line - 1);
                var lineTop = bottom - line * 9;
                if (!text.hasObfuscation) {
                    GuiGraphicsExt.drawString(graphics, font, text, left, lineTop, -1, false);
                }

                if (cache.numbers().size() >= line) {
                    GuiGraphicsExt.drawString(graphics, font, cache.numbers().get(line - 1), backgroundRight - cache.numberWidths().get(line - 1), lineTop, -1, false);
                }
            }
        });
    }
}
