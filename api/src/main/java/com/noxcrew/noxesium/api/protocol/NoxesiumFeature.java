package com.noxcrew.noxesium.api.protocol;

/**
 * A class that contains entries for each notable feature added to Noxesium, these features
 * are accompanied by a protocol version that indicates the lowest available protocol
 * version of Noxesium that supports this feature. This enum can be used to communicate
 * information about which features are available for usage based on which client is being
 * interacted with.
 */
public enum NoxesiumFeature {
    /**
     * Supports sending player heads in the %NCPH% syntax.
     */
    PLAYER_HEADS(2),
    /**
     * Supports sending player heads in the new %nox_uuid% syntax. The old syntax has been fully removed
     * as the mod has moved to API v1. The new syntax allows either a uuid or a full texture to be given.
     * This allows servers to send player heads of fake players or with cached skins.
     */
    ANONYMOUS_PLAYER_HEADS(3),
    /**
     * Adds support for the custom music server rule which allows servers to use the added core music and
     * game music sliders.
     */
    MUSIC_SERVER_RULE(3),
    /**
     * Supports the V1 API of Noxesium.
     */
    API_V1(3),
    ;

    private final int minProtocolVersion;

    NoxesiumFeature(int minProtocolVersion) {
        this.minProtocolVersion = minProtocolVersion;
    }

    /**
     * Returns the minimum protocol version of a client necessary
     * to support this feature.
     */
    public int getMinProtocolVersion() {
        return minProtocolVersion;
    }
}
