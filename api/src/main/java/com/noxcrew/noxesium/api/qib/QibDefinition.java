package com.noxcrew.noxesium.api.qib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;

/**
 * Defines the behavior of a qib. Qib stands for nothing, it's a silly word I made
 * up at some point to refer to the concept of an in-world interactable area.
 * <p>
 * Some examples of "qibs":
 * - A speed booster
 * - A jump pad
 * - A launch pad
 * - A booster ring
 * <p>
 * The qib system is intended to synchronize with a server-side implementation of
 * such objects, specifically this system is designed for MCC but it's made such that
 * the logic is entirely decided by the server. It's also to not give recreations the
 * easiest day of their life, but feel free to use this system yourself.
 * <p>
 * !! This system is currently a beta feature. This version is an initial testing version
 * of the system and may likely not be production ready. Expect breaking changes to happen
 * in future protocol versions.
 *
 * @param onEnter                   An effect triggered when a player enters a qib.
 * @param onLeave                   An effect triggered when a player leaves a qib.
 * @param whileInside               An effect triggered each client tick while inside a qib.
 * @param onJump                    An effect triggered when a player jumps while inside a qib.
 * @param triggerEnterLeaveOnSwitch Whether to trigger the enter & leave effects when moving to a different
 *                                  instance of the same qib definition.
 */
public record QibDefinition(
        @Nullable QibEffect onEnter,
        @Nullable QibEffect onLeave,
        @Nullable QibEffect whileInside,
        @Nullable QibEffect onJump,
        boolean triggerEnterLeaveOnSwitch) {

    /**
     * A GSON implementation that can serialize QibDefinition objects.
     */
    public static final Gson QIB_GSON = new GsonBuilder()
            .registerTypeAdapter(QibEffect.class, new QibEffectSerializer())
            .create();
}
