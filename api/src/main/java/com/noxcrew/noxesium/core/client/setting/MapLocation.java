package com.noxcrew.noxesium.core.client.setting;

/**
 * Changes the location where the map UI is displayed.
 */
public enum MapLocation {
    TOP,
    TOP_FLIPPED,

    // Legacy positions, v3+ lets you set the height yourself!
    BOTTOM,
    BOTTOM_FLIPPED;

    /**
     * Returns whether this location is on the bottom side.
     */
    public boolean isBottom() {
        return this == BOTTOM || this == BOTTOM_FLIPPED;
    }

    /**
     * Returns whether this location should be flipped.
     */
    public boolean isFlipped() {
        return this == TOP_FLIPPED || this == BOTTOM_FLIPPED;
    }
}
