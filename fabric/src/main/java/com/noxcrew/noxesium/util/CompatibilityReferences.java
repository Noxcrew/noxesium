package com.noxcrew.noxesium.util;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Holds references to which other mods are loaded or not.
 */
public class CompatibilityReferences {

    /**
     * Returns whether feather client is being used.
     * Prevents the experimental performance patches from being usable because of Feather's
     * movable UI components.
     */
    public static boolean isUsingFeatherClient() {
        return FabricLoader.getInstance().isModLoaded("feather");
    }
}
