package com.noxcrew.noxesium.api.component;

/**
 * A NoxesiumComponentHolder used on the client that receives it's data from a remote server.
 */
public interface RemoteNoxesiumComponentHolder extends NoxesiumComponentHolder {

    /**
     * Loads the data for the given component from the server.
     */
    public default void noxesium$loadComponent(NoxesiumComponentType<?> component, Object value) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    /**
     * Unsets any loaded data for the given component.
     */
    public default void noxesium$unsetComponent(NoxesiumComponentType<?> component) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    /**
     * Resets all loaded custom component data.
     */
    public default void noxesium$reloadComponents() {
        throw new UnsupportedOperationException("Unimplemented");
    }
}
