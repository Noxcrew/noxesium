package com.noxcrew.noxesium.api.protocol.rule;

/**
 * A static location to fetch all server rule indices.
 */
public class ServerRuleIndices {

    /**
     * Prevents the riptide trident's spin attack from colliding with any targets.
     */
    public static final int DISABLE_SPIN_ATTACK_COLLISIONS = 0;

    // Ids 1 and 2 have been removed.

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
     * Disables vanilla music from playing in the background.
     */
    public static final int DISABLE_VANILLA_MUSIC = 5;

    /**
     * Prevents boat on entity collisions on the client side.
     */
    public static final int DISABLE_BOAT_COLLISIONS = 6;

    // Id 7 has been removed.

    /**
     * Configures an item which will be used whenever the properties of
     * the player's hand are resolved. This applies to adventure mode
     * breaking/placing restrictions as well as tool modifications.
     */
    public static final int HAND_ITEM_OVERRIDE = 8;

    /**
     * Disables the UI optimizations temporarily which can be used to
     * temporarily allow using shader animated text.
     * <p>
     * Notice: This is a temporary server rule as the goal is to have
     * the UI optimizations not cause any issues, but they currently
     * don't support animated text with shaders.
     */
    public static final int DISABLE_UI_OPTIMIZATIONS = 9;

    /**
     * Moves the handheld map to be shown in the top left/right corner instead of
     * in the regular hand slot.
     */
    public static final int SHOW_MAP_IN_UI = 10;

    /**
     * Forces the client to run chunk updates immediately instead of deferring
     * them to the off-thread. Can be used to force a client to update the world
     * to avoid de-synchronizations on chunk updates.
     */
    public static final int DISABLE_DEFERRED_CHUNK_UPDATES = 11;

    /**
     * Defines a list of items to show in a custom creative tab.
     */
    public static final int CUSTOM_CREATIVE_ITEMS = 12;

    /**
     * Defines all known qib behaviors that can be triggered by players interacting with marked interaction entities.
     * These behaviors are defined globally to avoid large amounts of data sending.
     */
    public static final int QIB_BEHAVIORS = 13;

    /**
     * Allows the server to override the graphics mode used by the client.
     */
    public static final int OVERRIDE_GRAPHICS_MODE = 14;

    /**
     * Enables Noxesium patches to the trident which make it entirely client-sided
     * allowing for additional smoothness. The following changes need to be made
     * server side to support this:
     * - Remove entire block of riptide specific code that runs server-side after
     *   the Item Used statistic is given
     * - Override isAutoSpinAttack on the server to be true still while riptiding,
     *   but do not send the client any updates to the riptiding flag in the living
     *   entity data.
     * - Do not send the client updates about its own pose.
     * - Ignore all logic about using the auto spin attack as an attack.
     * - Add a sound effect called noxesium:trident_ready_indicator
     *
     * The effects this setting has:
     * - Makes the sound effect and camera POV (pose) change client-side
     * - Adds an indicator sound when the trident has been charged enough
     * - Adds coyote time to releasing the trident when briefly not in water
     *   or if released slightly too early (0.1s)
     */
    public static final int ENABLE_SMOOTHER_CLIENT_TRIDENT = 15;
}
