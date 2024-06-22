package com.noxcrew.noxesium.feature.ui.cache;

import net.minecraft.world.scores.Objective;
import org.jetbrains.annotations.Nullable;

/**
 * Stores information about the state of the tab list.
 */
public record TabListInformation(
        @Nullable Objective objective
) {}
