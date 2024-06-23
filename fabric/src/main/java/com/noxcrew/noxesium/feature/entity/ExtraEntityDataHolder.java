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
}
