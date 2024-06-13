package com.noxcrew.noxesium.feature.render.cache.gametime;

import com.noxcrew.noxesium.feature.render.cache.ElementInformation;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import org.jetbrains.annotations.Nullable;

/**
 * Stores information about the game time overlay element.
 */
public record GameTimeOverlayInformation(BakedComponent gameTime) implements ElementInformation {

    /**
     * The fallback contents if the game time overlay is hidden.
     */
    public static final GameTimeOverlayInformation EMPTY = new GameTimeOverlayInformation(BakedComponent.EMPTY);

    @Override
    public boolean isEmpty() {
        return this == EMPTY;
    }
}
