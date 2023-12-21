package com.noxcrew.noxesium.feature.render.cache.bossbar;

import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import net.minecraft.world.BossEvent;

/**
 * Stores information about a single boss bar.
 *
 * @param name     The name of the boss bar
 * @param overlay  The overlay of this bar
 * @param color    The color of this bar
 * @param progress The progress of this bar
 */
public record BossBar(
        BakedComponent name,
        BossEvent.BossBarOverlay overlay,
        BossEvent.BossBarColor color,
        float progress
) {
}
