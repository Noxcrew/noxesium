package com.noxcrew.noxesium.feature.ui.cache;

import net.minecraft.world.scores.Objective;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Stores information about the state of the scoreboard. We cache
 * this information as scoreboards are drawn each frame, so storing some
 * information about it between ticks is useful.
 */
public record ScoreboardInformation(
        @Nullable
        Objective objective,
        List<String> players,
        List<String> teams
) {

    /**
     * An empty scoreboard information object.
     */
    public static ScoreboardInformation EMPTY = new ScoreboardInformation(null, List.of(), List.of());
}
