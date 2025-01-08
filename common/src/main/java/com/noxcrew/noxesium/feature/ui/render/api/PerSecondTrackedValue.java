package com.noxcrew.noxesium.feature.ui.render.api;

/**
 * A value that we want to track every second.
 */
public class PerSecondTrackedValue {

    private long nextTime = (System.currentTimeMillis() / 1000 * 1000) + 1000;
    private int current = 0;
    private int last = 0;

    /**
     * Checks if the current value should be updated.
     */
    private void check() {
        var millis = System.currentTimeMillis();
        if (millis >= nextTime) {
            last = current;
            current = 0;

            // Jump the next time to the next second after now
            nextTime = (millis / 1000 * 1000) + 1000;
        }
    }

    /**
     * Returns the last value.
     */
    public int get() {
        check();
        return last;
    }

    /**
     * Increments this value.
     */
    public void increment() {
        check();
        current++;
    }
}
