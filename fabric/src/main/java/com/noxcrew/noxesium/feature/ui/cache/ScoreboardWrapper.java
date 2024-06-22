package com.noxcrew.noxesium.feature.ui.cache;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;

import java.util.ArrayList;

/**
 * Manages the current cache of the scoreboard.
 */
public class ScoreboardWrapper extends ElementWrapper {

    private ScoreboardInformation cache;

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
     * Creates new cached scoreboard information.
     */
    private ScoreboardInformation createCache(Player player) {
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

        var players = new ArrayList<String>();
        var teams = new ArrayList<String>();
        players.add(player.getScoreboardName());
        if (localPlayerTeam != null) {
            teams.add(localPlayerTeam.getName());
        }

        return new ScoreboardInformation(
                objective,
                players,
                teams
        );
    }

    @Override
    protected void render(GuiGraphics graphics, Minecraft minecraft, int screenWidth, int screenHeight, Font font, DeltaTracker deltaTracker) {
        // Update the cached information, we use this to know when to invalidate the currently shown scoreboard.
        cache = createCache(minecraft.player);
    }
}
