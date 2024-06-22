package com.noxcrew.noxesium.feature.ui.cache;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;

/**
 * Manages the current cache of the tab list.
 */
public class TabListWrapper extends ElementWrapper {

    private TabListInformation cache;

    /**
     * Returns whether the given objective is relevant to the current cache.
     * We compare against the exact instance of the objective for speed.
     */
    public boolean isObjectiveRelevant(Objective objective) {
        if (cache == null || cache.objective() == null) return false;
        return cache.objective() == objective;
    }

    @Override
    protected void render(GuiGraphics graphics, Minecraft minecraft, int screenWidth, int screenHeight, Font font, DeltaTracker deltaTracker) {
        // Update the cached information, we use this to know when to invalidate the currently shown scoreboard.
        var scoreboard = minecraft.level.getScoreboard();
        cache = new TabListInformation(scoreboard.getDisplayObjective(DisplaySlot.LIST));
    }

    /**
     * Returns the id of the latency symbol for the given player.
     */
    public int getLatencyBucket(int latency) {
        if (latency < 0) {
            return 0;
        } else if (latency < 150) {
            return 5;
        } else if (latency < 300) {
            return 4;
        } else if (latency < 600) {
            return 3;
        } else if (latency < 1000) {
            return 2;
        } else {
            return 1;
        }
    }
}
