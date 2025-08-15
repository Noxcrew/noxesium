package com.noxcrew.noxesium.api.component;

import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface injected into items, entities, the minecraft server, and block entities
 * that allows them to store additional components.
 */
public interface NoxesiumComponentHolder {

    /**
     * Returns the given component data on this holder.
     */
    @Nullable
    public default <T> T noxesium$getComponent(NoxesiumComponentType<T> component) {
        return null;
    }

    /**
     * Returns the given component data on this holder or the value supplied by the given default value supplier.
     */
    @NotNull
    public default <T> T noxesium$getComponentOr(NoxesiumComponentType<T> component, Supplier<T> defaultValue) {
        var value = noxesium$getComponent(component);
        if (value == null) {
            return defaultValue.get();
        } else {
            return value;
        }
    }

    /**
     * Returns the given component data on this holder wrapped in an optional.
     */
    @NotNull
    public default <T> Optional<T> noxesium$getOptionalComponent(NoxesiumComponentType<T> component) {
        return Optional.ofNullable(noxesium$getComponent(component));
    }

    /**
     * Returns whether this holder has the given component set.
     */
    public default boolean noxesium$hasComponent(NoxesiumComponentType<?> component) {
        return false;
    }
}
