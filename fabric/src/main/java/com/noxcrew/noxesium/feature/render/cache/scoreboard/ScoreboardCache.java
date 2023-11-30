package com.noxcrew.noxesium.feature.render.cache.scoreboard;

import com.noxcrew.noxesium.feature.render.cache.ElementCache;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import com.noxcrew.noxesium.feature.render.font.BakedComponentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Manages the current cache of the scoreboard.
 */
public class ScoreboardCache extends ElementCache<ScoreboardInformation> {

    private static final Comparator<PlayerScoreEntry> SCORE_DISPLAY_ORDER = Comparator.comparing(PlayerScoreEntry::value)
            .reversed()
            .thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER);
    private static ScoreboardCache instance;
    private static final int RIGHT = 3;

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
    protected ScoreboardInformation createCache(Minecraft minecraft, Font font) {
        var player = minecraft.player;
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
        var numberFormat = objective.numberFormatOrDefault(StyledFormat.SIDEBAR_DEFAULT);

        var players = new ArrayList<String>();
        var teams = new ArrayList<String>();
        players.add(player.getScoreboardName());
        if (localPlayerTeam != null) {
            teams.add(localPlayerTeam.getName());
        }

        // Get all player scores and sort them correctly
        var scores = new ArrayList<>(scoreboard.listPlayerScores(objective));
        scores.sort(SCORE_DISPLAY_ORDER);

        var lines = new ArrayList<BakedComponent>();
        var numbers = new ArrayList<BakedComponent>();

        var lowerLimit = scores.size() - 15;
        var headerBuilder = new BakedComponentBuilder(objective.getDisplayName(), font);
        headerBuilder.shadow = false;
        var baked = headerBuilder.build();
        var extraWidth = font.width(": ");
        var maxWidth = baked.width;

        for (var index = scores.size() - 1; index >= lowerLimit && index >= 0; index--) {
            var score = scores.get(index);
            if (score.isHidden()) {
                // If we skip a line we can continue iterating further down
                lowerLimit--;
                continue;
            }

            // We are iterating in reverse order, so we add at the start of the list.
            // We want to end up with the last 15 entries of scores.
            var playerTeam = scoreboard.getPlayersTeam(score.owner());
            players.add(score.owner());
            if (playerTeam != null) {
                teams.add(playerTeam.getName());
            }
            var text = PlayerTeam.formatNameForTeam(playerTeam, score.ownerName());
            var lineTextBuilder = new BakedComponentBuilder(text, font);
            lineTextBuilder.shadow = false;
            var bakedText = lineTextBuilder.build();
            lines.add(0, bakedText);

            // Update the maximum width we've found, if numbers are being omitted we
            // move the whole background right.
            var component = score.formatValue(numberFormat);
            var numberTextBuilder = new BakedComponentBuilder(component, font);
            numberTextBuilder.shadow = false;
            var bakedNumber = numberTextBuilder.build();
            maxWidth = Math.max(maxWidth, bakedText.width + (bakedNumber.width > 0 ? bakedNumber.width + extraWidth : 0));
            numbers.add(0, bakedNumber);
        }

        return new ScoreboardInformation(
                objective,
                players,
                teams,
                baked,
                lines,
                numbers,
                maxWidth
        );
    }

    @Override
    protected void render(GuiGraphics graphics, ScoreboardInformation cache, Minecraft minecraft, int screenWidth, int screenHeight, Font font, float partialTicks, boolean dynamic) {
        var height = cache.lines().size() * 9;
        var bottom = screenHeight / 2 + height / 3;
        var left = screenWidth - cache.maxWidth() - RIGHT;
        var backgroundRight = screenWidth - RIGHT + 2;
        var headerTop = bottom - cache.lines().size() * 9;

        if (!dynamic) {
            // Draw the header and its background
            var darkerBackground = minecraft.options.getBackgroundColor(0.4f);
            graphics.fill(left - 2, headerTop - 9 - 1, backgroundRight, headerTop - 1, darkerBackground);

            // Draw the entire regular background
            var background = minecraft.options.getBackgroundColor(0.3f);
            graphics.fill(left - 2, bottom, backgroundRight, headerTop - 1, background);
        }

        // Draw the header itself
        if (cache.header().shouldDraw(dynamic)) {
            cache.header().draw(graphics, font, left + cache.maxWidth() / 2 - cache.header().width / 2, headerTop - 9, -1);
        }

        // Line 1 here is the bottom line, this is because the
        // finalScores are sorted and we're getting them still
        // ordered ascending, so we want to display the last
        // score at the top.
        for (var line = 1; line <= cache.lines().size(); line++) {
            var text = cache.lines().get(line - 1);
            var lineTop = bottom - line * 9;

            if (text.shouldDraw(dynamic)) {
                text.draw(graphics, font, left, lineTop, -1);
            }

            // Only draw the numbers when buffered
            if (cache.numbers().size() >= line) {
                var num = cache.numbers().get(line - 1);
                if (num.shouldDraw(dynamic)) {
                    num.draw(graphics, font, backgroundRight - num.width, lineTop, -1);
                }
            }
        }
    }
}
