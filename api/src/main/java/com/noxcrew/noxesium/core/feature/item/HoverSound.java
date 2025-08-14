package com.noxcrew.noxesium.core.feature.item;

import java.util.Optional;
import net.kyori.adventure.key.Key;

/**
 * Defines a sound to play when hovering over an item stack.
 */
public record HoverSound(Optional<Sound> hoverOn, Optional<Sound> hoverOff, boolean onlyPlayInNonPlayerInventories) {

    /**
     * Stores information about an individual sound and its allowed pitch and volume.
     */
    public record Sound(Key sound, float volume, float pitchMin, float pitchMax) {}
}
