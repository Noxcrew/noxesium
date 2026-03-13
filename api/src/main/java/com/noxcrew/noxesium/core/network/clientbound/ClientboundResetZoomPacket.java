package com.noxcrew.noxesium.core.network.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.core.feature.EasingType;
import java.util.Optional;

/**
 * Resets the client's current zoom override.
 *
 * @param ticks      The amount of ticks to take to go back to default values.
 * @param easingType The easing function to use for the transition (only used if ticks is set and positive).
 */
public record ClientboundResetZoomPacket(Optional<Integer> ticks, EasingType easingType) implements NoxesiumPacket {}
