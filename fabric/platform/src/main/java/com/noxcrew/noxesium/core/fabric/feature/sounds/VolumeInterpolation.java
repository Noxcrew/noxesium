package com.noxcrew.noxesium.core.fabric.feature.sounds;

/**
 * Manages volume interpolation of a Noxesium sound instance.
 */
public class VolumeInterpolation {
    private final float startingVolume;
    private final float volumeRange;
    private final float totalTicks;
    private float ticks;

    public VolumeInterpolation(float startingVolume, float endingVolume, int ticks) {
        this.startingVolume = startingVolume;
        this.volumeRange = endingVolume - startingVolume;
        this.totalTicks = (float) ticks;
        this.ticks = (float) ticks;
    }

    /**
     * Ticks this interpolation, returning the new desired volume of the sound.
     */
    public Float tick() {
        ticks--;
        if (ticks < 0) {
            // On the -1-th tick we return null to stop interpolating.
            return null;
        } else {
            // Calculate the volume again based on how many ticks have passed.
            return (1f - (ticks / totalTicks)) * volumeRange + startingVolume;
        }
    }
}
