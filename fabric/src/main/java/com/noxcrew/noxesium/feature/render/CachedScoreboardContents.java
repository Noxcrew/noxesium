package com.noxcrew.noxesium.feature.render;

import com.google.common.cache.Cache;
import com.mojang.datafixers.util.Pair;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Stores information about a single state of the scoreboard. We cache
 * this information as scoreboards are drawn each frame, so storing some
 * information about it between ticks is useful.
 *
 * @param header       The header text.
 * @param lines        The final lines to be drawn.
 * @param numberWidths The widths of each row's number.
 * @param headerWidth  The width of the scoreboard's header.
 * @param maxWidth     The maximum width of the scoreboard.
 * @param drawNumbers  Whether the numbers should be drawn.
 */
public record CachedScoreboardContents(
        @Nullable
        Objective objective,
        List<String> players,
        List<String> teams,
        Component header,
        List<Pair<Score, Component>> lines,
        List<Integer> numberWidths,
        int headerWidth,
        int maxWidth,
        boolean drawNumbers
) {

    /**
     * The fallback contents if the scoreboard is empty.
     */
    public static final CachedScoreboardContents EMPTY = new CachedScoreboardContents(null, List.of(), List.of(), Component.empty(), List.of(), List.of(), 0, 0, false);

    private static CachedScoreboardContents current;

    /**
     * Clears the currently cached scoreboard contents. The frequency at which this is irrelevant as it's assumed
     * a server, even if it's constantly updating the scoreboard, cannot do so more frequently than a fast enough
     * client would be attempting to render it. Any lowering of scoreboard re-caching from client fps to server
     * update rate is a noticeable improvement.
     * <p>
     * We do tend to only clear the cache if relevant, however we always clear if any changes are made
     * to the scores of players, even if said players' scores are not visible on the scoreboard. This could
     * be optimized further but is deemed unnecessary. If your scoreboard has a lot of hidden players it's
     * probably not one that heavily needs optimization.
     */
    public static void clearCache() {
        current = null;
    }

    /**
     * Returns whether the given player is relevant to the current cache.
     */
    public static boolean isPlayerRelevant(String player) {
        if (current == null) return false;
        return current.players.contains(player);
    }

    /**
     * Returns whether the given team is relevant to the current cache.
     */
    public static boolean isTeamRelevant(String team) {
        if (current == null) return false;
        return current.teams.contains(team);
    }

    /**
     * Returns whether the given objective is relevant to the current cache.
     * We compare against the exact instance of the objective for speed.
     */
    public static boolean isObjectiveRelevant(Objective objective) {
        if (current == null || current.objective == null) return false;
        return current.objective == objective;
    }

    /**
     * Returns the current cached scoreboard contents.
     */
    public static CachedScoreboardContents getCachedScoreboardContents() {
        if (current == null) {
            current = cacheScoreboard();
        }
        return current;
    }

    /**
     * Creates newly cached scoreboard content information.
     * Depends on the following information:
     * - Current resource pack configuration
     * - Current scoreboard contents of objective
     * - The draw numbers setting
     * - Any visual properties of the associated teams (team of client player and teams of all players in objective)
     */
    public static CachedScoreboardContents cacheScoreboard() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return CachedScoreboardContents.EMPTY;
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
            return CachedScoreboardContents.EMPTY;
        }

        var drawNumbers = !ServerRules.DISABLE_SCOREBOARD_NUMBER_RENDERING.getValue();
        var font = Minecraft.getInstance().font;

        var players = new ArrayList<String>();
        var teams = new ArrayList<String>();
        players.add(player.getScoreboardName());
        if (localPlayerTeam != null) {
            teams.add(localPlayerTeam.getName());
        }

        var scores = (List<Score>) scoreboard.getPlayerScores(objective);
        var finalScores = new ArrayList<Pair<Score, Component>>();
        var numberWidths = new ArrayList<Integer>();

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
            finalScores.add(0, Pair.of(score, text));

            // Update the maximum width we've found, if numbers are being omitted we move the whole
            // background right.
            var number = "" + ChatFormatting.RED + score.getScore();
            var numberWidth = drawNumbers ? font.width(number) : 0;
            maxWidth = Math.max(maxWidth, font.width(text) + extraWidth + numberWidth);
            numberWidths.add(0, numberWidth);
        }

        return new CachedScoreboardContents(
                objective,
                players,
                teams,
                title,
                finalScores,
                numberWidths,
                headerWidth,
                maxWidth,
                drawNumbers
        );
    }
}
