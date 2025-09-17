package com.noxcrew.noxesium.core.registry;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.RegistryCollection;
import com.noxcrew.noxesium.api.util.GraphicsMode;
import com.noxcrew.noxesium.api.util.UiConstraints;
import com.noxcrew.noxesium.api.util.Unit;
import java.util.List;

/**
 * Stores all common Noxesium game component types.
 */
public class CommonGameComponentTypes {
    public static final RegistryCollection<NoxesiumComponentType<?>> INSTANCE =
            new RegistryCollection<>(NoxesiumRegistries.GAME_COMPONENTS);

    /**
     * If set, disables the riptide spin attack on the trident from colliding with any entities,
     * useful for non-pvp mini-games where the trident is used as a movement tool.
     */
    public static NoxesiumComponentType<Unit> DISABLE_SPIN_ATTACK_COLLISIONS =
            register("disable_spin_attack_collisions", Unit.class);

    /**
     * An integer pixel amount to vertically offset the HUD held item name.
     * Useful for avoiding overlapping action bar icons. Positive values move the text up.
     */
    public static NoxesiumComponentType<Integer> HELD_ITEM_NAME_OFFSET =
            register("held_item_name_offset", Integer.class);

    /**
     * If set, the client player is prevented from using any mouse inputs to
     * move their camera.
     */
    public static NoxesiumComponentType<Unit> CAMERA_LOCKED = register("camera_locked", Unit.class);

    /**
     * If set, vanilla background music is fully disabled.
     */
    public static NoxesiumComponentType<Unit> DISABLE_VANILLA_MUSIC = register("disable_vanilla_music", Unit.class);

    /**
     * If set, disables boat collision on the client side, useful for movement games involving
     * boats and other entities in one area. Similar mechanism must exist server side to prevent
     * lag-backs with this enabled.
     */
    public static NoxesiumComponentType<Unit> DISABLE_BOAT_COLLISIONS = register("disable_boat_collisions", Unit.class);

    /**
     * If set, forces the client to run chunk updates immediately instead of deferring
     * them to the off-thread. Can be used to force a client to update the world
     * to avoid de-synchronizations on chunk updates.
     */
    public static NoxesiumComponentType<Unit> DISABLE_DEFERRED_CHUNK_UPDATES =
            register("disable_deferred_chunk_updates", Unit.class);

    /**
     * Moves the handheld map to be shown in the top left/right corner instead of
     * in the regular hand slot when set to true or fully hides when set to false.
     * Map follows client setting if value is not set.
     */
    public static NoxesiumComponentType<Boolean> SHOW_MAP_IN_UI = register("show_map_in_ui", Boolean.class);

    /**
     * Allows the server to override the graphics mode used by the client.
     */
    public static NoxesiumComponentType<GraphicsMode> OVERRIDE_GRAPHICS_MODE =
            register("override_graphics_mode", GraphicsMode.class);

    /**
     * If set, enables a custom smoother riptide trident implementation. Requires server-side adjustments.
     */
    public static NoxesiumComponentType<Unit> ENABLE_SMOOTHER_CLIENT_TRIDENT =
            register("enable_smoother_client_trident", Unit.class);

    /**
     * Sets the amount of ticks the riptide has coyote time for.
     */
    public static NoxesiumComponentType<Integer> RIPTIDE_COYOTE_TIME = register("riptide_coyote_time", Integer.class);

    /**
     * If set, enables the ability to pre-charge riptide tridents.
     */
    public static NoxesiumComponentType<Unit> RIPTIDE_PRE_CHARGING = register("riptide_pre_charging", Unit.class);

    /**
     * Restricts available debug options available to the player.
     */
    public static NoxesiumComponentType<List<Integer>> RESTRICT_DEBUG_OPTIONS =
            register("restrict_debug_options", List.class);

    /**
     * Allows tripwires and note blocks to have server authoritative updates. This means the client does not
     * attempt to make any local block state changes for these blocks.
     */
    public static NoxesiumComponentType<Unit> SERVER_AUTHORITATIVE_BLOCK_UPDATES =
            register("server_authoritative_block_updates", Unit.class);

    /**
     * Sets constraints on the rendering size of the boss bar.
     */
    public static NoxesiumComponentType<Unit> BOSS_BAR_CONSTRAINTS =
            register("boss_bar_constraints", UiConstraints.class);

    /**
     * Sets constraints on the rendering size of the scoreboard.
     */
    public static NoxesiumComponentType<Unit> SCOREBOARD_CONSTRAINTS =
            register("scoreboard_constraints", UiConstraints.class);

    /**
     * Sets constraints on the rendering size of the tab list.
     */
    public static NoxesiumComponentType<Unit> TAB_LIST_CONSTRAINTS =
            register("tab_list_constraints", UiConstraints.class);

    /**
     * Sets constraints on the rendering size of the action bar.
     */
    public static NoxesiumComponentType<Unit> ACTION_BAR_CONSTRAINTS =
            register("action_bar_constraints", UiConstraints.class);

    /**
     * Sets constraints on the rendering size of the title text UI, includes
     * both the title and its subtitle.
     */
    public static NoxesiumComponentType<Unit> TEXT_UI_CONSTRAINTS =
            register("text_ui_constraints", UiConstraints.class);

    /**
     * Registers a new component type to the registry.
     */
    private static <R, T> NoxesiumComponentType<T> register(String key, Class<R> clazz) {
        return NoxesiumRegistries.register(INSTANCE, NoxesiumReferences.NAMESPACE, key, (Class<T>) clazz);
    }
}
