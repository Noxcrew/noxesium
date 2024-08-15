package com.noxcrew.noxesium.api.qib;

import java.util.List;

/**
 * Defines an effect a qib can create.
 */
public sealed interface QibEffect {

    /**
     * Triggers a list of effects.
     */
    public record Multiple(
        List<QibEffect> effects
    ) implements QibEffect {
    }

    /**
     * Waits for an amount of ticks before executing effect.
     */
    public record Wait(
        int ticks,
        QibEffect effect
    ) implements QibEffect {
    }

    /**
     * Continues based on some condition.
     */
    public record Conditional(
        QibCondition condition,
        boolean value,
        QibEffect effect
    ) implements QibEffect {
    }

    /**
     * Plays a sound effect.
     */
    public record PlaySound(
        String namespace,
        String path,
        float volume,
        float pitch
    ) implements QibEffect {
    }

    /**
     * Applies a client-authoritative potion effect.
     * This is a custom potion effect that will only
     * be known to the client and can be overridden
     * by any server-side effect.
     * <p>
     * Using this will completely de-sync any anti-cheat plugins
     * you are using as well as make Bukkit very confused so
     * use this with caution and restraint!
     */
    public record GivePotionEffect(
        String namespace,
        String path,
        int duration,
        int amplifier,
        boolean ambient,
        boolean visible,
        boolean showIcon
    ) implements QibEffect {
    }

    /**
     * Removes a client-authoritative potion effect.
     * This only affects effects added by a [GivePotionEffect] effect.
     */
    public record RemovePotionEffect(
        String namespace,
        String path
    ) implements QibEffect {
    }

    /**
     * Forces the player to make an instant relative movement.
     */
    public record Move(
        double x,
        double y,
        double z
    ) implements QibEffect {
    }

    /**
     * Adds the given velocity.
     */
    public record AddVelocity(
        double x,
        double y,
        double z
    ) implements QibEffect {
    }

    /**
     * Sets the velocity to a set value, removing
     * any previous velocity the client had.
     */
    public record SetVelocity(
        double x,
        double y,
        double z
    ) implements QibEffect {
    }

    /**
     * Sets the velocity to a set value based on a yaw
     * and pitch value. Yaw and pitch can be relative which
     * makes the resulting velocity based on the current yaw/pitch
     * of the player. Values are clamped at limit (also at -limit).
     */
    public record SetVelocityYawPitch(
        double yaw,
        boolean yawRelative,
        double pitch,
        boolean pitchRelative,
        double strength,
        double limit
    ) implements QibEffect {
    }
}
