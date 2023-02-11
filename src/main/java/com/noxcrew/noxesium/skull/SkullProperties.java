package com.noxcrew.noxesium.skull;


import java.util.UUID;

/**
 * Defines the properties of a skull.
 */
public record SkullProperties(UUID uuid, String texture, boolean grayscale, float scale, int advance, int ascent) {

    public SkullProperties(SkullContents contents) {
        // Only include a texture if there is no uuid available.
        this(contents.getUuid(), contents.getUuid() == null ? contents.getTexture() : null, contents.isGrayscale(), contents.getScale(), contents.getAdvance(), contents.getAscent());
    }
}
