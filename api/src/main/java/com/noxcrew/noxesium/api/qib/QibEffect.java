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
     * Performs some action that edits the player's state.
     */
    public record UpdateState(
            QibState state
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
     * Applies a potion effect. It is expected that the
     * server also gives out this potion effect itself to
     * avoid de-synchronization. This acts as a prediction only!
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
     * Removes a potion effect.
     */
    public record RemovePotionEffect(
            String namespace,
            String path
    )  implements QibEffect {
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
