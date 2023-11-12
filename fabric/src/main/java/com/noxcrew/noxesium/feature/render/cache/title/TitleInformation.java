package com.noxcrew.noxesium.feature.render.cache.title;

import com.noxcrew.noxesium.feature.render.cache.ElementInformation;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import org.jetbrains.annotations.Nullable;

/**
 * Stores information about the state of the title.
 *
 * @param title The current title
 * @param subtitle The current subtitle
 */
public record TitleInformation(
        BakedComponent title,
        @Nullable BakedComponent subtitle,
        boolean fading
) implements ElementInformation {

    /**
     * The fallback contents if the title is absent.
     */
    public static final TitleInformation EMPTY = new TitleInformation(BakedComponent.EMPTY, null, false);
}
