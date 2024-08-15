package com.noxcrew.noxesium.feature.entity;

/**
 * Defines an extension to LivingEntity.
 */
public interface LivingEntityExtension {

    /**
     * Triggers coyote time on releasing a riptide trident.
     */
    public default void noxesium$triggerTridentCoyoteTime() {
    }

    /**
     * Resets coyote time for the riptide trident.
     */
    public default void noxesium$resetTridentCoyoteTime() {
    }
}
