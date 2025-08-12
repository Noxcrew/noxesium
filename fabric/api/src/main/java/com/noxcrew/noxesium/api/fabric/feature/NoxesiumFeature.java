package com.noxcrew.noxesium.api.fabric.feature;

/**
 * A feature in Noxesium that has hooks called when the feature is
 * constructed and destroyed.
 */
public interface NoxesiumFeature {

    /**
     * Called when this feature is registered.
     */
    default void onRegister() {}

    /**
     * Called when this feature is unregistered.
     */
    default void onUnregister() {}
}
