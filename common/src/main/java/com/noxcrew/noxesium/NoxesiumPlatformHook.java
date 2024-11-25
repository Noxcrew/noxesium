package com.noxcrew.noxesium;

import java.nio.file.Path;

/**
 * Provides various hooks with platform specific implementations.
 */
public interface NoxesiumPlatformHook {

    /**
     * Returns the configuration directory.
     */
    Path getConfigDirectory();

    /**
     * Returns whether a given mod name is loaded.
     */
    boolean isModLoaded(String modName);

    /**
     * Returns the version of Noxesium being used.
     */
    String getNoxesiumVersion();
}
