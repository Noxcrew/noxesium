package com.noxcrew.noxesium.util;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Holds references to which other mods are loaded or not.
 */
public class CompatibilityReferences {

    /**
     * Returns whether cloth configs are being used.
     * Enables a custom config UI where experimental performance changes can be enabled.
     */
    public static boolean isUsingClothConfig() {
        return FabricLoader.getInstance().isModLoaded("cloth-config");
    }

    /**
     * Returns whether feather client is being used.
     * Prevents the experimental performance patches from being usable because of Feather's
     * movable UI components.
     */
    public static boolean isUsingFeatherClient() {
        return FabricLoader.getInstance().isModLoaded("feather");
    }
}
