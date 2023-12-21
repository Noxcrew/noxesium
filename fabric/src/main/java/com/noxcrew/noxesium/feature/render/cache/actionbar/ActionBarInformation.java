package com.noxcrew.noxesium.feature.render.cache.actionbar;

import com.noxcrew.noxesium.feature.render.cache.ElementInformation;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;

/**
 * Stores information about the state of the action bar.
 *
 * @param component The current text
 * @param alpha The alpha value of the text
 */
public record ActionBarInformation(
        BakedComponent component,
        int alpha
) implements ElementInformation {

    /**
     * The fallback contents if the action bar is absent.
     */
    public static final ActionBarInformation EMPTY = new ActionBarInformation(BakedComponent.EMPTY, 255);

    @Override
    public boolean isEmpty() {
        return this == EMPTY;
    }
}
