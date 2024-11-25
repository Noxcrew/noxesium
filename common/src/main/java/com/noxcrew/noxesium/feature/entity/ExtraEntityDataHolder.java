package com.noxcrew.noxesium.feature.entity;

import com.noxcrew.noxesium.feature.rule.ClientServerRule;

/**
 * An interface injected into Entity that allows them to store extra data.
 */
public interface ExtraEntityDataHolder {

    /**
     * Returns the extra data stored under the given rule.
     */
    public default <T> T noxesium$getExtraData(ClientServerRule<T> rule) {
        return rule.getValue();
    }

    /**
     * Returns whether the server has sent over extra data for this rule.
     */
    public default boolean noxesium$hasExtraData(ClientServerRule<?> rule) {
        return false;
    }

    /**
     * Sets extra data for the given [rule] to [value].
     */
    public default void noxesium$setExtraData(ClientServerRule<?> rule, Object value) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    /**
     * Resets the extra data for the given [rule].
     */
    public default void noxesium$resetExtraData(ClientServerRule<?> rule) {
        throw new UnsupportedOperationException("Unimplemented");
    }
}
