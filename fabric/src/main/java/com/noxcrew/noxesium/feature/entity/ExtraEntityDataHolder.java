package com.noxcrew.noxesium.feature.entity;

import com.noxcrew.noxesium.feature.rule.ClientServerRule;

/**
 * An interface injected into Entity that allows them to store extra data.
 */
public interface ExtraEntityDataHolder {

    /**
     * Returns the extra data stored under the given rule.
     */
    public default <T> T getExtraData(ClientServerRule<T> rule) {
        return rule.getValue();
    }

    /**
     * Sets extra data for the given [rule] to [value].
     */
    public default void setExtraData(ClientServerRule<?> rule, Object value) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    /**
     * Resets the extra data for the given [rule].
     */
    public default void resetExtraData(ClientServerRule<?> rule) {
        throw new UnsupportedOperationException("Unimplemented");
    }
}
