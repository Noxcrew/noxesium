package com.noxcrew.noxesium.feature.skull;

import java.util.UUID;

/**
 * Defines the properties of a skull.
 */
public record SkullProperties(UUID uuid, String texture, boolean grayscale, float scale, int advance, int ascent) {

    public SkullProperties(SkullSprite contents) {
        this(
                contents.getUuid(),
                contents.getTexture(),
                contents.isGrayscale(),
                contents.getScale(),
                contents.getAdvance(),
                contents.getAscent());
    }
}
