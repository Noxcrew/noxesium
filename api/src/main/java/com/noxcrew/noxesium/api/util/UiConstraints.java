package com.noxcrew.noxesium.api.util;

/**
 * Sets constraints on a UI element as defined by a server.
 *
 * @param baseValue       The default base value to use if the client does not change it.
 * @param multipliedValue A scalar to apply to the client's final value.
 * @param minValue        The minimum value the client may use.
 * @param maxValue        The maximum value the client may use.
 */
public record UiConstraints(double baseValue, double multipliedValue, double minValue, double maxValue) {}
