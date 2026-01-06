package com.noxcrew.noxesium.core.registry;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.RegistryCollection;
import com.noxcrew.noxesium.api.util.Unit;
import com.noxcrew.noxesium.core.feature.GuiConstraints;
import com.noxcrew.noxesium.core.feature.GuiElement;
import java.util.List;
import java.util.Map;

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
     * If set, enables client authoritative riptide tridents. Requires server-side code which is available
     * in the provided Paper implementation.
     */
    public static NoxesiumComponentType<Unit> CLIENT_AUTHORITATIVE_RIPTIDE_TRIDENTS =
            register("client_authoritative_riptide_trident", Unit.class);

    /**
     * Sets the amount of ticks the client authoritative riptide tridents have coyote time for.
     * This allows players to release the trident even after leaving the water for a few ticks
     * which makes it feel smoother when on the water surface.
     */
    public static NoxesiumComponentType<Integer> RIPTIDE_COYOTE_TIME = register("riptide_coyote_time", Integer.class);

    /**
     * If set, enables the ability to pre-charge client authoritative riptide tridents.
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
     * Sets constraints on the rendering size of GUI elements.
     */
    public static NoxesiumComponentType<Map<GuiElement, GuiConstraints>> GUI_CONSTRAINTS =
            register("gui_constraints", Map.class);

    /**
     * If set, enables client authoritative elytra usage. Requires server-side code which is available
     * in the provided Paper implementation.
     * <p>
     * This will make the server unable to control elytra states.
     */
    public static NoxesiumComponentType<Unit> CLIENT_AUTHORITATIVE_ELYTRA =
            register("client_authoritative_elytra", Unit.class);

    /**
     * Sets the amount of ticks the client authoritative elytra has coyote time for.
     * This allows players to hop around with the elytra which is possible in vanilla
     * on higher ping.
     */
    public static NoxesiumComponentType<Double> ELYTRA_COYOTE_TIME = register("elytra_coyote_time", Double.class);

    /**
     * Registers a new component type to the registry.
     */
    private static <R, T> NoxesiumComponentType<T> register(String key, Class<R> clazz) {
        return NoxesiumRegistries.register(INSTANCE, NoxesiumReferences.NAMESPACE, key, (Class<T>) clazz);
    }
}
