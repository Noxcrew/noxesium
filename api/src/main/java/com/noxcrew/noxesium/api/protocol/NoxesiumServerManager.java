package com.noxcrew.noxesium.api.protocol;

import com.noxcrew.noxesium.api.protocol.rule.ServerRule;

/**
 * The interfaces for a server-side implementation of a Noxesium manager. This is set up to interact with
 * multiple versions of clients at the same time. It is expected that this manager re-routes calls into
 * legacy implementations of a manager. The methods in this manager represent all functionality a Noxesium
 * server is expected to be able to use to interact with a client.
 */
public interface NoxesiumServerManager<PlayerT> {

    /**
     * Returns an object that modifies the server rule with the given rule index
     * for the given player.
     *
     * @param player The player object to fetch the rule for.
     * @param ruleIndex The id of the rule from ServerRuleIndices.
     * @return An object that stores the current rule state for the given player.
     */
    <T> ServerRule<T, ?> getServerRule(PlayerT player, int ruleIndex);
}
