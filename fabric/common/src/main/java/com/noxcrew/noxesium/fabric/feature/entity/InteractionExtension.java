package com.noxcrew.noxesium.fabric.feature.entity;

/**
 * Defines an extension to Interaction for spatial tracking.
 */
public interface InteractionExtension {

    /**
     * Marks down that this entity has been added to the world.
     */
    public default void noxesium$markAddedToWorld() {}

    /**
     * Returns whether this entity has been added to the world.
     */
    public default boolean noxesium$isInWorld() {
        return false;
    }
}
