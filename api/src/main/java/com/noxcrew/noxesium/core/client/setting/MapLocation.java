package com.noxcrew.noxesium.core.client.setting;

/**
 * Changes the location where the map UI is displayed.
 */
public enum MapLocation {
    TOP,
    BOTTOM,
    TOP_FLIPPED,
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
