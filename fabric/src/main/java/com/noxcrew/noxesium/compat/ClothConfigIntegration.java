package com.noxcrew.noxesium.compat;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.client.gui.screens.Screen;

/**
 * Supplies a menu created by Cloth Config if it exists.
 */
public class ClothConfigIntegration {

    /**
     * Registers the configuration file.
     */
    public static void register() {
        AutoConfig.register(NoxesiumConfig.class, Toml4jConfigSerializer::new);
    }

    /**
     * Returns the configuration screen with the given parent.
     */
    public static Screen getMenu(Screen parent) {
        return AutoConfig.getConfigScreen(NoxesiumConfig.class, parent).get();
    }

    /**
     * Returns the instance of the Noxesium configuration.
     */
    public static NoxesiumConfig getConfig() {
        return AutoConfig.getConfigHolder(NoxesiumConfig.class).getConfig();
    }

    public static boolean getFpsOverlay() {
        return getConfig().fpsOverlay;
    }

    public static boolean getExperimentalPerformancePatches() {
        return getConfig().experimentalPerformanceChanges;
    }
}
