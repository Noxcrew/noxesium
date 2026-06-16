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
 * @param onAttack                  An effect triggered when an item is used to attack with this qib effect.
 * @param onUse                     An effect triggered when an item is used with this qib effect.
 * @param triggerEnterLeaveOnSwitch Whether to trigger the enter & leave effects when moving to a different
 *                                  instance of the same qib definition.
 */
public record QibDefinition(
        @Nullable QibEffect onEnter,
        @Nullable QibEffect onLeave,
        @Nullable QibEffect whileInside,
        @Nullable QibEffect onJump,
        @Nullable QibEffect onAttack,
        @Nullable QibEffect onUse,
        boolean triggerEnterLeaveOnSwitch) {

    /**
     * A GSON implementation that can serialize QibDefinition objects.
     */
    public static final Gson QIB_GSON = new GsonBuilder()
            .registerTypeAdapter(Vector3f.class, new VectorSerializer())
            .registerTypeAdapter(QibEffect.class, new QibEffectSerializer())
            .create();

    /**
     * Creates a new qib definition builder.
     */
    public static QibDefinition.Builder builder() {
        return new Builder();
    }

    /**
     * Assists in creating qib definitions.
     */
    public static class Builder {
        private QibEffect onEnter = null;
        private QibEffect onLeave = null;
        private QibEffect whileInside = null;
        private QibEffect onJump = null;
        private QibEffect onAttack = null;
        private QibEffect onUse = null;
        private boolean triggerEnterLeaveOnSwitch = true;

        /**
         * Defines the effect to trigger when entering this qib.
         */
        public Builder onEnter(QibEffect effect) {
            this.onEnter = effect;
            return this;
        }

        /**
         * Defines the effect to trigger when leaving this qib.
         */
        public Builder onLeave(QibEffect effect) {
            this.onLeave = effect;
            return this;
        }

        /**
         * Defines the effect to trigger every tick while inside this qib.
         */
        public Builder whileInside(QibEffect effect) {
            this.whileInside = effect;
            return this;
        }

        /**
         * Defines the effect to trigger when jumping in this qib.
         */
        public Builder onJump(QibEffect effect) {
            this.onJump = effect;
            return this;
        }

        /**
         * Defines the effect to trigger when attacking with an item with this qib attached.
         */
        public Builder onAttack(QibEffect effect) {
            this.onAttack = effect;
            return this;
        }

        /**
         * Defines the effect to trigger when using an item with this qib attached.
         */
        public Builder onUse(QibEffect effect) {
            this.onUse = effect;
            return this;
        }

        /**
         * Defines whether the qib should trigger enter and leave effects when
         * switching between instances.
         */
        public Builder setTriggerEnterLeaveOnSwitch(boolean value) {
            this.triggerEnterLeaveOnSwitch = value;
            return this;
        }

        /**
         * Assembles the qib definition.
         */
        public QibDefinition build() {
            return new QibDefinition(onEnter, onLeave, whileInside, onJump, onAttack, onUse, triggerEnterLeaveOnSwitch);
        }
    }
}
