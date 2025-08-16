package com.noxcrew.noxesium.core.feature.item;

import java.util.Optional;
import net.kyori.adventure.key.Key;

/**
 * Allows customising whether a slot can be hovered over and which sprites to draw when doing so.
 */
public record Hoverable(boolean hoverable, Optional<Key> frontSprite, Optional<Key> backSprite) {}
