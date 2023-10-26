package com.noxcrew.noxesium.feature.render.cache.bossbar;

import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.world.BossEvent;

/**
 * Stores information about a single boss bar.
 *
 * @param name      The name of the boss bar
 * @param barWidth  The width of the boss bar's name
 * @param bar       The current bar object which holds the progress
 * @param overlay   The overlay of this bar
 * @param color     The color of this bar
 * @param animating Whether the bar's progress is animating right now
 */
public record BossBar(
        BakedComponent name,
        int barWidth,
        LerpingBossEvent bar,
        BossEvent.BossBarOverlay overlay,
        BossEvent.BossBarColor color,
        boolean animating
) {
}
