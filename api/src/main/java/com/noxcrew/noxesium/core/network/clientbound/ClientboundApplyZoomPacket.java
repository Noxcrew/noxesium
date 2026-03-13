package com.noxcrew.noxesium.core.network.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.core.feature.EasingType;
import java.util.Optional;

/**
 * Sent by a server to control the client's zoom by changing their FOV.
 *
 * @param zoom               The target zoom level (multiplier). (1.0 = normal FOV, <1.0 = zoomed in, >1.0 = zoomed out)
 * @param transitionTicks    Duration of the zoom transition in ticks (20 ticks = 1 second). If `0` the transition is instant.
 * @param easingType         The easing function to use for the transition.
 * @param keepHandStationary Whether the hand should follow the zoom level.
 * @param fov                The FOV to target when zooming, if not given the zoom level is relative to the user's FOV, if it is given
 *                           the zoom adapts to end at the given zoom level given to this FOV setting.
 */
public record ClientboundApplyZoomPacket(
        float zoom, int transitionTicks, EasingType easingType, boolean keepHandStationary, Optional<Integer> fov)
        implements NoxesiumPacket {}
