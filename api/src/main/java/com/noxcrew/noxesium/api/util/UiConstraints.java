package com.noxcrew.noxesium.api.util;

/**
 * Sets constraints on a UI element as defined by a server.
 *
 * @param scalar   A scalar to apply to the client's selected value.
 * @param minValue The minimum value the client may use.
 * @param maxValue The maximum value the client may use.
 */
public record UiConstraints(double scalar, double minValue, double maxValue) {}
