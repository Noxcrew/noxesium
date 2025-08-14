package com.noxcrew.noxesium.core.registry;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.RegistryCollection;
import com.noxcrew.noxesium.api.util.GraphicsMode;
import com.noxcrew.noxesium.api.util.Unit;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundMouseButtonClickPacket;
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
            register("disable_spin_attack_collisions");

    /**
     * An integer pixel amount to vertically offset the HUD held item name.
     * Useful for avoiding overlapping action bar icons. Positive values move the text up.
     */
    public static NoxesiumComponentType<Integer> HELD_ITEM_NAME_OFFSET = register("held_item_name_offset");

    /**
     * If set, the client player is prevented from using any mouse inputs to
     * move their camera.
     */
    public static NoxesiumComponentType<Unit> CAMERA_LOCKED = register("camera_locked");

    /**
     * If set, vanilla background music is fully disabled.
     */
    public static NoxesiumComponentType<Unit> DISABLE_VANILLA_MUSIC = register("disable_vanilla_music");

    /**
     * If set, disables boat collision on the client side, useful for movement games involving
     * boats and other entities in one area. Similar mechanism must exist server side to prevent
     * lag-backs with this enabled.
     */
    public static NoxesiumComponentType<Unit> DISABLE_BOAT_COLLISIONS = register("disable_boat_collisions");

    /**
     * If set, forces the client to run chunk updates immediately instead of deferring
     * them to the off-thread. Can be used to force a client to update the world
     * to avoid de-synchronizations on chunk updates.
     */
    public static NoxesiumComponentType<Unit> DISABLE_DEFERRED_CHUNK_UPDATES =
            register("disable_deferred_chunk_updates");

    /**
     * Moves the handheld map to be shown in the top left/right corner instead of
     * in the regular hand slot when set to true or fully hides when set to false.
     * Map follows client setting if value is not set.
     */
    public static NoxesiumComponentType<Boolean> SHOW_MAP_IN_UI = register("show_map_in_ui");

    /**
     * Allows the server to override the graphics mode used by the client.
     */
    public static NoxesiumComponentType<GraphicsMode> OVERRIDE_GRAPHICS_MODE = register("override_graphics_mode");

    /**
     * If set, enables a custom smoother riptide trident implementation. Requires server-side adjustments.
     */
    public static NoxesiumComponentType<Unit> ENABLE_SMOOTHER_CLIENT_TRIDENT =
            register("enable_smoother_client_trident");

    /**
     * Sets the amount of ticks the riptide has coyote time for.
     */
    public static NoxesiumComponentType<Integer> RIPTIDE_COYOTE_TIME = register("riptide_coyote_time");

    /**
     * If set, enables the ability to pre-charge riptide tridents.
     */
    public static NoxesiumComponentType<Unit> RIPTIDE_PRE_CHARGING = register("riptide_pre_charging");

    /**
     * Restricts available debug options available to the player.
     */
    public static NoxesiumComponentType<List<Integer>> RESTRICT_DEBUG_OPTIONS = register("restrict_debug_options");

    /**
     * Allows tripwires and note blocks to have server authoritative updates. This means the client does not
     * attempt to make any local block state changes for these blocks.
     */
    public static NoxesiumComponentType<Unit> SERVER_AUTHORITATIVE_BLOCK_UPDATES =
            register("server_authoritative_block_updates");

    /**
     * If set, the client sends a {@link ServerboundMouseButtonClickPacket} for every
     * mouse click at most once per tick per type. Clicks more frequent than once per
     * tick are ignored.
     */
    public static NoxesiumComponentType<Unit> REPORT_MOUSE_CLICKS = register("report_mouse_clicks");

    /**
     * Registers a new component type to the registry.
     */
    private static <T> NoxesiumComponentType<T> register(String key) {
        return NoxesiumRegistries.register(INSTANCE, NoxesiumReferences.NAMESPACE, key);
    }
}
