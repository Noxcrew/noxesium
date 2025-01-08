package com.noxcrew.noxesium.feature.ui.render.api;

/**
 * A helper to track if a task should be executed yet when it wants
 * to reach some goal per second.
 */
public class PerSecondRepeatingTask {

    private double frequency;
    private long nextInvocation;
    private long nanosPerInvocation;

    public PerSecondRepeatingTask(double frequency) {
        this(frequency, System.nanoTime());
    }

    public PerSecondRepeatingTask(double frequency, long nanoTime) {
        changeFrequency(frequency, nanoTime);
        if (canInvoke(nanoTime)) {
            nextInvocation -= nanosPerInvocation;
        }
    }

    /**
     * Returns the rounded framerate of this task.
     */
    public int getFramerate() {
        return (int) Math.floor(frequency);
    }

    /**
     * Returns whether this task can currently be invoked.
     */
    public boolean canInvoke() {
        return canInvoke(System.nanoTime());
    }

    /**
     * Returns whether this task can currently be invoked.
     */
    public boolean canInvoke(long nanoTime) {
        if (nanoTime > nextInvocation) {
            nextInvocation = (nanoTime / nanosPerInvocation * nanosPerInvocation) + nanosPerInvocation;
            return true;
        }
        return false;
    }

    /**
     * Changes the frequency of this task.
     */
    public void changeFrequency(double value) {
        changeFrequency(value, System.nanoTime());
    }

    /**
     * Changes the frequency of this task.
     */
    public void changeFrequency(double value, long nanoTime) {
        this.frequency = value;
        this.nanosPerInvocation = (long) Math.floor(((1 / value) * 1000000000));
        this.nextInvocation = (nanoTime / nanosPerInvocation * nanosPerInvocation) + nanosPerInvocation;
    }

    /**
     * Returns how frequently this task runs per second.
     */
    public double getFrequency() {
        return frequency;
    }
}
