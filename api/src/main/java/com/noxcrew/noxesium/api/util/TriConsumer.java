package com.noxcrew.noxesium.api.util;

/**
 * A simple consumer with three input parameters.
 */
@FunctionalInterface
public interface TriConsumer<A, B, C> {
    void accept(A a, B b, C C);
}
