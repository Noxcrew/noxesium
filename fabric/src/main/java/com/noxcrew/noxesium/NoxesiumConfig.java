package com.noxcrew.noxesium;

import com.noxcrew.noxesium.compat.ClothConfigIntegration;
import net.minecraft.SharedConstants;

/**
 * Stores Noxesium's configured values.
 */
public class NoxesiumConfig {

    /**
     * Whether to enable experimental performance patches.
     */
    public static Boolean enableExperimentalPatches = null;

    /**
     * Whether to show the FPS overlay.
     */
    public static boolean fpsOverlay = false;

    /**
     * Returns whether experimental performance are enabled in the configuration.
     */
    public static boolean hasConfiguredPerformancePatches() {
        // Never allow the custom patches when using feather
        if (CompatibilityReferences.isUsingFeatherClient()) {
            return false;
        }

        // Check with Cloth Config if it's configured
        if (CompatibilityReferences.isUsingClothConfig()) {
            return ClothConfigIntegration.getExperimentalPerformancePatches();
        }

        // Fallback when testing without Cloth Config in IDE
        return SharedConstants.IS_RUNNING_IN_IDE;
    }

    /**
     * Whether the experimental performance patches should be used.
     */
    public static boolean shouldDisableExperimentalPerformancePatches() {
        if (hasConfiguredPerformancePatches()) {
            if (enableExperimentalPatches != null) {
                return !enableExperimentalPatches;
            }
            return false;
        }
        return true;
    }

    /**
     * Whether the fps overlay should be shown.
     */
    public static boolean shouldShowFpsOverlay() {
        if (CompatibilityReferences.isUsingClothConfig()) {
            return ClothConfigIntegration.getFpsOverlay();
        }
        return fpsOverlay;
    }
}
