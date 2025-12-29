package com.noxcrew.noxesium.core.fabric.feature.entity;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * Defines an extension to LivingEntity for client-side qib related features.
 */
public interface LivingEntityExtension {

    /**
     * Adds a client-side potion effect [instance].
     */
    public default void noxesium$addClientsidePotionEffect(MobEffectInstance instance) {}

    /**
     * Removes a client side potion [effect].
     */
    public default void noxesium$removeClientsidePotionEffect(Holder<MobEffect> effect) {}

    /**
     * Updates the client-side potion effects.
     */
    public default void noxesium$updateClientsidePotionEffects() {}

    /**
     * Clears the client-side potion effects.
     */
    public default void noxesium$clearClientsidePotionEffects() {}

    /**
     * Returns whether a player has trident coyote time.
     */
    public default boolean noxesium$hasTridentCoyoteTime() {
        return false;
    }

    /**
     * Resets coyote time for the riptide trident.
     */
    public default void noxesium$resetTridentCoyoteTime() {}

    /**
     * Returns whether a player is in the auto spin attack.
     */
    public default boolean noxesium$hasAutoSpinAttack() {
        return false;
    }
}
