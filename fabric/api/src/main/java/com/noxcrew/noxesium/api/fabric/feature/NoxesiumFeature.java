package com.noxcrew.noxesium.api.fabric.feature;

/**
 * A feature in Noxesium that has hooks called when the feature is
 * constructed and destroyed.
 */
public abstract class NoxesiumFeature {

    private boolean registered;

    /**
     * Returns whether this feature is registered.
     */
    public boolean isRegistered() {
        return registered;
    }

    /**
     * Registers the feature.
     */
    public final void register() {
        if (registered) return;
        registered = true;
        onRegister();
    }

    /**
     * Unregisters the feature.
     */
    public final void unregister() {
        if (!registered) return;
        registered = false;
        onUnregister();
    }

    /**
     * Called when this feature is registered.
     */
    public void onRegister() {}

    /**
     * Called when this feature is unregistered.
     */
    public void onUnregister() {}
}
