package com.noxcrew.noxesium.api.component;

import org.jetbrains.annotations.Nullable;

/**
 * Stores the old and new values of a component being changed along with
 * the receiver of the new value.
 */
public record ComponentChangeContext<T, R>(@Nullable T oldValue, @Nullable T newValue, R receiver) {}
