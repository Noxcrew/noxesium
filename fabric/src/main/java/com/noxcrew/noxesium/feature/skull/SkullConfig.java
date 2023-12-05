package com.noxcrew.noxesium.feature.skull;

import java.util.concurrent.CompletableFuture;

/**
 * A holding object for the texture to show for a skull and its properties.
 */
public record SkullConfig(CompletableFuture<String> texture, SkullProperties properties) {
}
