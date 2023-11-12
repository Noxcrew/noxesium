package com.noxcrew.noxesium.compat;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

/**
 * Stores the Noxesium configuration.
 */
@Config(name = "noxesium")
public class NoxesiumConfig implements ConfigData {
    boolean fpsOverlay = false;
    boolean experimentalPerformanceChanges = false;
}
