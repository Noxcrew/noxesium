package com.noxcrew.noxesium.core.network.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.core.feature.EasingType;

/**
 * Sent by a server to control the client's zoom/FOV.
 *
 * @param zoom               The target zoom level (multiplier). (1.0 = normal FOV, <1.0 = zoomed in, >1.0 = zoomed out)
 * @param transitionTicks    Duration of the zoom transition in ticks (20 ticks = 1 second). 0 = instant
 * @param easingType         The easing function to use for the transition
 * @param lockClientFov      Whether to prevent the client from manually changing their FOV while zoomed
 * @param keepHandStationary Whether the hand should follow the zoom level
 * @param reset              If true, resets to normal FOV (1.0) and unlocks FOV control. When true, zoom value is ignored
 */
public record ClientboundZoomPacket(
        float zoom,
        int transitionTicks,
        EasingType easingType,
        boolean lockClientFov,
        boolean keepHandStationary,
        boolean reset)
        implements NoxesiumPacket {}
