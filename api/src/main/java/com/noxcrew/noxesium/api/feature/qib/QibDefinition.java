package com.noxcrew.noxesium.api.feature.qib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

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
 * Qibs can also be applied to items to create your own custom items such as the lunge
 * spear, these use the onAttack effect type.
 *
 * @param onEnter                   An effect triggered when a player enters a qib.
 * @param onLeave                   An effect triggered when a player leaves a qib.
 * @param whileInside               An effect triggered each client tick while inside a qib.
 * @param onJump                    An effect triggered when a player jumps while inside a qib.
 * @param onAttack                  An effect triggered when an item is used with this qib effect.
 * @param triggerEnterLeaveOnSwitch Whether to trigger the enter & leave effects when moving to a different
 *                                  instance of the same qib definition.
 */
public record QibDefinition(
        @Nullable QibEffect onEnter,
        @Nullable QibEffect onLeave,
        @Nullable QibEffect whileInside,
        @Nullable QibEffect onJump,
        @Nullable QibEffect onAttack,
        boolean triggerEnterLeaveOnSwitch) {

    /**
     * A GSON implementation that can serialize QibDefinition objects.
     */
    public static final Gson QIB_GSON = new GsonBuilder()
            .registerTypeAdapter(Vector3f.class, new VectorSerializer())
            .registerTypeAdapter(QibEffect.class, new QibEffectSerializer())
            .create();
}
