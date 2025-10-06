package com.noxcrew.noxesium.core.fabric.util;

/**
 * A feature that runs an async background task.
 */
public interface BackgroundTaskFeature {
    /**
     * Called from the async background task.
     */
    public void runAsync();
}
