package com.noxcrew.noxesium.api.component;

/**
 * A NoxesiumComponentHolder that can also have its components mutated. Should be implemented
 * instead of just being a holder on the server-side.
 */
public interface MutableNoxesiumComponentHolder extends NoxesiumComponentHolder {

    /**
     * Returns the given component data on this holder.
     */
    public default <T> void noxesium$setComponent(NoxesiumComponentType<T> component, T value) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    /**
     * Clears all components from this holder.
     */
    public default void noxesium$clearComponents() {
        throw new UnsupportedOperationException("Unimplemented");
    }
}
