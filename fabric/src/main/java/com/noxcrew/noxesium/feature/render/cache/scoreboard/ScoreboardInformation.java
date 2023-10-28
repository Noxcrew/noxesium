package com.noxcrew.noxesium.feature.render.cache.scoreboard;

import com.noxcrew.noxesium.feature.render.cache.ElementInformation;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Stores information about a single state of the scoreboard. We cache
 * this information as scoreboards are drawn each frame, so storing some
 * information about it between ticks is useful.
 *
 * @param objective    The relevant scoreboard objective.
 * @param players      The players whose team this scoreboard depends on.
 * @param teams        The teams whose properties this scoreboard depends on.
 * @param header       The header text.
 * @param lines        The final lines to be drawn.
 * @param numbers      The text to be drawn for the numbers.
 * @param maxWidth     The maximum width of the scoreboard.
 */
public record ScoreboardInformation(
        @Nullable
        Objective objective,
        List<String> players,
        List<String> teams,
        BakedComponent header,
        List<BakedComponent> lines,
        List<BakedComponent> numbers,
        int maxWidth,
        boolean hasObfuscation
) implements ElementInformation {

    /**
     * The fallback contents if the scoreboard is empty.
     */
    public static final ScoreboardInformation EMPTY =
            new ScoreboardInformation(null, List.of(), List.of(), BakedComponent.EMPTY, List.of(), List.of(), 0, false);
}
