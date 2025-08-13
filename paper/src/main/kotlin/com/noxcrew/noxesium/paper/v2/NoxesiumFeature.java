package com.noxcrew.noxesium.paper.v2;

/**
 * A class that contains entries for each notable feature added to Noxesium, these features
 * are accompanied by a protocol version that indicates the lowest available protocol
 * version of Noxesium that supports this feature. This enum can be used to communicate
 * information about which features are available for usage based on which client is being
 * interacted with.
 */
public enum NoxesiumFeature {
    /**
     * A generic feature that can be used to check if Noxesium is installed in general.
     */
    ANY(0),
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
    /**
     * Adds support for disabling boat on entity collisions.
     */
    BOAT_COLLISIONS_RULE(4),
    /**
     * Allows usage of the custom sounds packets.
     */
    CUSTOM_SOUNDS(4),
    /**
     * Allows usage of the optimized UI rendering-related server rules.
     */
    OPTIMIZED_UI(4),
    /**
     * Allows disabling the optimized UI server rule by servers.
     */
    DISABLE_OPTIMIZED_UI_SERVER_RULE(5),
    /**
     * Allows the hand item to be overridden.
     *
     * @see [ServerRuleIndices.HAND_ITEM_OVERRIDE]
     */
    OVERRIDABLE_HAND_ITEM(6),
    /**
     * Supports the V2 API of Noxesium.
     */
    API_V2(6),
    /**
     * Supports new features added for MCC when updating to 1.21.
     *
     * @see [NoxesiumMod.RAW_MODEL_TAG]
     * @see [ServerRuleIndices.SHOW_MAP_IN_UI]
     * @see [ServerRuleIndices.DISABLE_DEFERRED_CHUNK_UPDATES]
     * @see [ServerRuleIndices.CUSTOM_CREATIVE_ITEMS]
     */
    NEW_MCC_FEATURES(7),
    /**
     * Added support for the extra entity data system.
     */
    EXTRA_ENTITY_DATA_SYSTEM(7),
    /**
     * Added support to let the server decide the Graphics options.
     */
    SERVER_DECIDES_GRAPHICS(8),
    /**
     * Finalised client qib feature to make it functional.
     */
    STABLE_CLIENT_QIBS(9),
    /**
     * Various bugfixes to client qib feature.
     */
    BUGFIXED_CLIENT_QIBS(10),
    /**
     * Fixes serialization of item stacks in rules.
     */
    FIXED_ITEM_STACK_SERIALIZATION(11),
    /**
     * Added support for ModifyVelocity and RemoveAllPotionEffects QibEffects.
     */
    NEW_QIB_EFFECTS(13),
    /**
     * Adds an option to pre-charge tridents.
     */
    TRIDENT_PRE_CHARGE_OPTION(13),
    /**
     * Added support for the ClientboundOpenLinkPacket.
     */
    OPEN_LINK_PACKET(14),
    /**
     * Adds syntax to component styles to render text at an arbitrary x/y offset.
     */
    COMPONENT_OFFSET_SYNTAX(15),
    /**
     * Changes item stack rules to use a non-registry based serializer that works
     * across minor versions.
     */
    UNIVERSAL_ITEM_SERIALIZERS(16),
    /**
     * Adds custom RGB glowing colors to entities.
     */
    CUSTOM_GLOW_COLOR(17),
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
