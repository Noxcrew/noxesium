package com.noxcrew.noxesium.skull;

import java.util.concurrent.CompletableFuture;

/**
 * Defines the properties of a skull.
 */
public record SkullConfig(CompletableFuture<String> texture, boolean grayscale, int advance, int ascent, float scale) {
}
