package com.noxcrew.noxesium.feature;

import com.noxcrew.noxesium.NoxesiumModule;

/**
 * Manages overriding chunk updates to be blocking while in Hole in the Wall.
 */
public class OverrideChunkUpdates implements NoxesiumModule {

    private boolean overriding = false;

    /**
     * Returns whether this module should currently be enabled.
     */
    public boolean shouldOverride() {
        return overriding;
    }

    /**
     * Updates whether this module is currently enabled.
     */
    public void updateState(boolean state) {
        overriding = state;
    }

    @Override
    public void onQuitServer() {
        overriding = false;
    }
}
