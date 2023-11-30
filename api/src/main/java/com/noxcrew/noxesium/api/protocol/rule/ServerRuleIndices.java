package com.noxcrew.noxesium.api.protocol.rule;

/**
 * A static location to fetch all server rule indices.
 */
public class ServerRuleIndices {

    /**
     * Prevents the riptide trident's spin attack from colliding with any targets.
     */
    public static final int DISABLE_SPIN_ATTACK_COLLISIONS = 0;

    /**
     * Allows the "CanPlaceOn" tag to be defined for a player regardless of items.
     */
    public static final int GLOBAL_CAN_PLACE_ON = 1;

    /**
     * Allows the "CanDestroy" tag to be defined for a player regardless of items.
     */
    public static final int GLOBAL_CAN_DESTROY = 2;

    /**
     * Adds an offset to the action bar text displayed that shows the name
     * of the held item.
     */
    public static final int HELD_ITEM_NAME_OFFSET = 3;

    /**
     * Prevents the player from moving their camera.
     */
    public static final int CAMERA_LOCKED = 4;

    /**
     * Allows the custom core music and game music sliders to be edited
     * by clients and used by the server.
     */
    public static final int ENABLE_CUSTOM_MUSIC = 5;

    /**
     * Allows server to prevent boat on entity collisions on the client side.
     */
    public static final int DISABLE_BOAT_COLLISIONS = 6;

    // Rule id 7 is taken by a now removed rule.
}
