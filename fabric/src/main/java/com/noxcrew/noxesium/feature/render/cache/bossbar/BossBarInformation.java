package com.noxcrew.noxesium.feature.render.cache.bossbar;

import com.noxcrew.noxesium.feature.render.cache.ElementInformation;

import java.util.List;

/**
 * Stores information about a single state of the boss bar.
 */
public record BossBarInformation(
        List<BossBar> bars
) implements ElementInformation {

    /**
     * The fallback contents if the boss bar is empty.
     */
    public static final BossBarInformation EMPTY =
            new BossBarInformation(List.of());

    @Override
    public boolean isEmpty() {
        return this == EMPTY;
    }
}
