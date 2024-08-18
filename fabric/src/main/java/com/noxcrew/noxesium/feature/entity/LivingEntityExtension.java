package com.noxcrew.noxesium.feature.entity;

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
    public default void noxesium$addClientsidePotionEffect(MobEffectInstance instance) {

    }

    /**
     * Removes a client side potion [effect].
     */
    public default void noxesium$removeClientsidePotionEffect(Holder<MobEffect> effect) {
    }

    /**
     * Updates the client-side potion effects.
     */
    public default void noxesium$updateClientsidePotionEffects() {
    }

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
