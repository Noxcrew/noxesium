package com.noxcrew.noxesium.core.fabric.feature.entity;

/**
 * Defines an extension to LivingEntity for the client-side elytra.
 */
public interface FallFlyingEntityExtension {

    /**
     * Starts fall flying for this entity.
     */
    public default void noxesium$startFallFlying() {}

    /**
     * Stops fall flying for this entity immediately.
     */
    public default void noxesium$stopFallFlying() {}
}
