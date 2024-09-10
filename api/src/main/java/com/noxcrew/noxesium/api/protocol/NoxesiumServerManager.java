package com.noxcrew.noxesium.api.protocol;

import com.noxcrew.noxesium.api.protocol.rule.ServerRule;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * The interfaces for a server-side implementation of a Noxesium manager. This is set up to interact with
 * multiple versions of clients at the same time. It is expected that this manager re-routes calls into
 * legacy implementations of a manager. The methods in this manager represent all functionality a Noxesium
 * server is expected to be able to use to interact with a client.
 *
 * It is generally expected that servers take the burden of updating first. Clients will change to expect
 * the latest protocol, where possible supporting older deprecated packets. Servers should continue implementing
 * legacy support for older versions going back as far as they allow to join, however.
 */
public interface NoxesiumServerManager<PlayerT> {

    /**
     * Returns an object that modifies the server rule with the given rule index
     * for the given player.
     *
     * @param player    The player object to fetch the rule for.
     * @param ruleIndex The id of the rule from ServerRuleIndices.
     * @return An object that stores the current rule state for the given player, or `null` if the player is not using Noxesium or
     * not on a version that supports this rule.
     */
    @Nullable <T> ServerRule<T, ?> getServerRule(PlayerT player, int ruleIndex);

    /**
     * +
     * Returns the client settings of the given player.
     *
     * @param player The player object to fetch the settings for.
     * @return The last settings object received for the player, or `null`.
     */
    @Nullable
    ClientSettings getClientSettings(PlayerT player);

    /**
     * +
     * Returns the client settings of the given player.
     *
     * @param playerId The uuid of the player to fetch the settings for.
     * @return The last settings object received for the player, or `null`.
     */
    @Nullable
    ClientSettings getClientSettings(UUID playerId);

    /**
     * Returns whether the player uses a version of Noxesium that supports the given
     * feature.
     *
     * @param player  The player object to fetch the settings for.
     * @param feature The feature to check if the player has access.
     * @return Whether the player can use the feature.
     */
    default boolean isUsingNoxesium(PlayerT player, NoxesiumFeature feature) {
        var version = getProtocolVersion(player);
        if (version == null) return false;
        return version >= feature.getMinProtocolVersion();
    }

    /**
     * Returns whether the player uses a version of Noxesium that supports the given
     * feature.
     *
     * @param playerId The uuid of the player to fetch the settings for.
     * @param feature  The feature to check if the player has access.
     * @return Whether the player can use the feature.
     */
    default boolean isUsingNoxesium(UUID playerId, NoxesiumFeature feature) {
        var version = getProtocolVersion(playerId);
        if (version == null) return false;
        return version >= feature.getMinProtocolVersion();
    }

    /**
     * Returns the protocol version used by the given player.
     *
     * @param player The player object to fetch the protocol version of.
     * @return The protocol version of the player, or `null`.
     */
    Integer getProtocolVersion(PlayerT player);

    /**
     * Returns the protocol version used by the given player.
     *
     * @param playerId The uuid of the player to fetch the protocol version of.
     * @return The protocol version of the player, or `null`.
     */
    Integer getProtocolVersion(UUID playerId);
}
