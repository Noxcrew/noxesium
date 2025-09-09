package com.noxcrew.noxesium.api.component;

import org.jetbrains.annotations.Nullable;

/**
 * A NoxesiumComponentHolder that can also have its components mutated. Should be implemented
 * instead of just being a holder on the server-side.
 */
public interface MutableNoxesiumComponentHolder extends NoxesiumComponentHolder {

    /**
     * Sets the given component on this holder.
     */
    public default <T> void noxesium$setComponent(NoxesiumComponentType<T> component, @Nullable T value) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    /**
     * Resets the given component on this holder.
     */
    public default <T> void noxesium$resetComponent(NoxesiumComponentType<T> component) {
        noxesium$setComponent(component, null);
    }

    /**
     * Clears all components from this holder.
     */
    public default void noxesium$clearComponents() {
        throw new UnsupportedOperationException("Unimplemented");
    }
}
