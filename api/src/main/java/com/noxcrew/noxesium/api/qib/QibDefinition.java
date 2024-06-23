package com.noxcrew.noxesium.api.qib;

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
 *
 * @param onEnter              An effect triggered when a player enters a qib.
 * @param whileInside          An effect triggered each client tick while inside a qib.
 * @param onJump               An effect triggered when a player jumps while inside a qib.
 * @param triggerEnterOnSwitch Whether to trigger the enter effect when moving to a different
 *                             instance of the same qib definition.
 */
public record QibDefinition(
        @Nullable
        QibEffect onEnter,
        @Nullable
        QibEffect whileInside,
        @Nullable
        QibEffect onJump,
        boolean triggerEnterOnSwitch
) {
}
