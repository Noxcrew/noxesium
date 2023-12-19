package com.noxcrew.noxesium.feature.render.cache.fps;

import com.noxcrew.noxesium.feature.render.cache.ElementInformation;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import org.jetbrains.annotations.Nullable;

/**
 * Stores information about the fps overlay element.
 */
public record FpsOverlayInformation(
        BakedComponent fps,
        @Nullable BakedComponent noxesium
        ) implements ElementInformation {

    /**
     * The fallback contents if the fps overlay is hidden.
     */
    public static final FpsOverlayInformation EMPTY = new FpsOverlayInformation(BakedComponent.EMPTY, null);

    @Override
    public boolean isEmpty() {
        return this == EMPTY;
    }
}
