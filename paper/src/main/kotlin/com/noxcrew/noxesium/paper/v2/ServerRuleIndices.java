package com.noxcrew.noxesium.paper.v2;

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

    // Id 9 has been removed.

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
     * - Remove the release using code on the server-side for players.
     * - Replace logic to detect when a player riptides with listening to the `riptide` packet.
     * - Do not send the client updates about its own pose.
     * - Ignore all logic about using the auto spin attack as an attack.
     * - Add a sound effect called noxesium:trident.ready_indicator.
     * <p>
     * The effects this setting has:
     * - Makes the sound effect and camera POV (pose) change client-side
     * - Adds an indicator sound when the trident has been charged enough
     * - Adds coyote time to releasing the trident when briefly not in water
     * - Changes held item renderer to have less motion when riptiding
     */
    public static final int ENABLE_SMOOTHER_CLIENT_TRIDENT = 15;

    /**
     * Disables the map showing as a UI element. Can be used to hide it during loading screens.
     */
    public static final int DISABLE_MAP_UI = 16;

    /**
     * Sets the amount of ticks the riptide has coyote time for.
     */
    public static final int RIPTIDE_COYOTE_TIME = 17;

    /**
     * Allows a trident to always be charged. This defaults to false to match vanilla behavior.
     * Pre-charging tridents can be beneficial if you design maps where you jump into water as it
     * lets you start charging mid-air before reaching the water allowing for more fluid movement.
     *
     * Due to the slowdown while charging this partially balances out, and you may feel like it is
     * more skillful to time it precisely such that you start charging the perfect time before touching
     * water.
     *
     * This does not match vanilla behavior and will give anyone using this an advantage to players
     * playing on a vanilla client. Historically though this was a mechanic on custom trident implementations
     * because they use the regular throwing tridents which have no restrictions, and because blocking
     * pre-charging makes it take longer for laggy players to be detected as having touched the water.
     *
     * If combined with [ENABLE_SMOOTHER_CLIENT_TRIDENT] the ready indicator is only played when you
     * are able to release the trident (touch the water) even if you have charged it longer.
     */
    public static final int RIPTIDE_PRE_CHARGING = 18;

    /**
     * Restricts available debug options available to the player.
     */
    public static final int RESTRICT_DEBUG_OPTIONS = 19;
}
