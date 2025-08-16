package com.noxcrew.noxesium.paper

import com.noxcrew.noxesium.paper.ServerRuleIndices.ENABLE_SMOOTHER_CLIENT_TRIDENT

/**
 * A static location to fetch all server rule indices.
 */
public object ServerRuleIndices {
    /**
     * Prevents the riptide trident's spin attack from colliding with any targets.
     */
    public const val DISABLE_SPIN_ATTACK_COLLISIONS: Int = 0

    /**
     * Adds an offset to the action bar text displayed that shows the name
     * of the held item.
     */
    public const val HELD_ITEM_NAME_OFFSET: Int = 3

    /**
     * Prevents the player from moving their camera.
     */
    public const val CAMERA_LOCKED: Int = 4

    /**
     * Disables vanilla music from playing in the background.
     */
    public const val DISABLE_VANILLA_MUSIC: Int = 5

    /**
     * Prevents boat on entity collisions on the client side.
     */
    public const val DISABLE_BOAT_COLLISIONS: Int = 6

    /**
     * Configures an item which will be used whenever the properties of
     * the player's hand are resolved. This applies to adventure mode
     * breaking/placing restrictions as well as tool modifications.
     */
    public const val HAND_ITEM_OVERRIDE: Int = 8

    /**
     * Moves the handheld map to be shown in the top left/right corner instead of
     * in the regular hand slot.
     */
    public const val SHOW_MAP_IN_UI: Int = 10

    /**
     * Forces the client to run chunk updates immediately instead of deferring
     * them to the off-thread. Can be used to force a client to update the world
     * to avoid de-synchronizations on chunk updates.
     */
    public const val DISABLE_DEFERRED_CHUNK_UPDATES: Int = 11

    /**
     * Defines a list of items to show in a custom creative tab.
     */
    public const val CUSTOM_CREATIVE_ITEMS: Int = 12

    /**
     * Defines all known qib behaviors that can be triggered by players interacting with marked interaction entities.
     * These behaviors are defined globally to avoid large amounts of data sending.
     */
    public const val QIB_BEHAVIORS: Int = 13

    /**
     * Allows the server to override the graphics mode used by the client.
     */
    public const val OVERRIDE_GRAPHICS_MODE: Int = 14

    /**
     * Enables Noxesium patches to the trident which make it entirely client-sided
     * allowing for additional smoothness. The following changes need to be made
     * server side to support this:
     * - Remove the release using code on the server-side for players.
     * - Replace logic to detect when a player riptides with listening to the `riptide` packet.
     * - Do not send the client updates about its own pose.
     * - Ignore all logic about using the auto spin attack as an attack.
     * - Add a sound effect called noxesium:trident.ready_indicator.
     *
     *
     * The effects this setting has:
     * - Makes the sound effect and camera POV (pose) change client-side
     * - Adds an indicator sound when the trident has been charged enough
     * - Adds coyote time to releasing the trident when briefly not in water
     * - Changes held item renderer to have less motion when riptiding
     */
    public const val ENABLE_SMOOTHER_CLIENT_TRIDENT: Int = 15

    /**
     * Disables the map showing as a UI element. Can be used to hide it during loading screens.
     */
    public const val DISABLE_MAP_UI: Int = 16

    /**
     * Sets the amount of ticks the riptide has coyote time for.
     */
    public const val RIPTIDE_COYOTE_TIME: Int = 17

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
    public const val RIPTIDE_PRE_CHARGING: Int = 18

    /**
     * Restricts available debug options available to the player.
     */
    public const val RESTRICT_DEBUG_OPTIONS: Int = 19

    /**
     * Allows tripwires and note blocks to have server authoritative updates. This means the client does not
     * attempt to make any local block state changes for these blocks.
     */
    public const val SERVER_AUTHORITATIVE_BLOCK_UPDATES: Int = 20
}
