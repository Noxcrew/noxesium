package com.noxcrew.noxesium.feature.entity;

/**
 * Extends the guardian and end crystal states with a beam color.
 */
public interface BeamColorStateExtension {

    default Integer noxesium$getBeamColor() {
        throw new UnsupportedOperationException("Unimplemented");
    }

    default Integer noxesium$getBeamColorFade() {
        throw new UnsupportedOperationException("Unimplemented");
    }

    default void noxesium$setBeamColor(Integer color, Integer fade) {
    }
}
