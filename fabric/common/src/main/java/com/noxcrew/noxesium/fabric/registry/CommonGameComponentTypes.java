package com.noxcrew.noxesium.fabric.registry;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.fabric.component.NoxesiumComponentListener;
import com.noxcrew.noxesium.api.fabric.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumExtraCodecs;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumExtraStreamCodecs;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumRegistries;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.key.Key;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;

/**
 * Stores all Noxesium game component types.
 */
public class CommonGameComponentTypes {

    /**
     * If set, disables the riptide spin attack on the trident from colliding with any entities,
     * useful for non-pvp mini-games where the trident is used as a movement tool.
     */
    public static NoxesiumComponentType<Unit> DISABLE_SPIN_ATTACK_COLLISIONS =
            register("disable_spin_attack_collisions", Unit.CODEC, Unit.STREAM_CODEC);

    /**
     * An integer pixel amount to vertically offset the HUD held item name.
     * Useful for avoiding overlapping action bar icons. Positive values move the text up.
     */
    public static NoxesiumComponentType<Integer> HELD_ITEM_NAME_OFFSET =
            register("held_item_name_offset", Codec.INT, ByteBufCodecs.INT);

    /**
     * If set, the client player is prevented from using any mouse inputs to
     * move their camera.
     */
    public static NoxesiumComponentType<Unit> CAMERA_LOCKED = register("camera_locked", Unit.CODEC, Unit.STREAM_CODEC);

    /**
     * If set, vanilla background music is fully disabled.
     */
    public static NoxesiumComponentType<Unit> DISABLE_VANILLA_MUSIC =
            register("disable_vanilla_music", Unit.CODEC, Unit.STREAM_CODEC);

    /**
     * If set, disables boat collision on the client side, useful for movement games involving
     * boats and other entities in one area. Similar mechanism must exist server side to prevent
     * lag-backs with this enabled.
     */
    public static NoxesiumComponentType<Unit> DISABLE_BOAT_COLLISIONS =
            register("disable_boat_collisions", Unit.CODEC, Unit.STREAM_CODEC);

    /**
     * Allows overriding the item that is used when resolving the capabilities of an empty item slot.
     */
    public static NoxesiumComponentType<ItemStack> HAND_ITEM_OVERRIDE =
            register("hand_item_override", ItemStack.OPTIONAL_CODEC, ItemStack.OPTIONAL_STREAM_CODEC);

    /**
     * If set, forces the client to run chunk updates immediately instead of deferring
     * them to the off-thread. Can be used to force a client to update the world
     * to avoid de-synchronizations on chunk updates.
     */
    public static NoxesiumComponentType<Unit> DISABLE_DEFERRED_CHUNK_UPDATES =
            register("disable_deferred_chunk_updates", Unit.CODEC, Unit.STREAM_CODEC);

    /**
     * Defines a list of items to show in a custom creative tab.
     */
    public static NoxesiumComponentType<List<ItemStack>> CUSTOM_CREATIVE_ITEMS = register(
            "custom_creative_items",
            Codec.list(ItemStack.OPTIONAL_CODEC),
            ByteBufCodecs.collection(ArrayList::new, ItemStack.OPTIONAL_STREAM_CODEC));

    /**
     * Moves the handheld map to be shown in the top left/right corner instead of
     * in the regular hand slot when set to true or fully hides when set to false.
     * Map follows client setting if value is not set.
     */
    public static NoxesiumComponentType<Boolean> SHOW_MAP_IN_UI =
            register("show_map_in_ui", Codec.BOOL, ByteBufCodecs.BOOL);

    /**
     * Allows the server to override the graphics mode used by the client.
     */
    public static NoxesiumComponentType<GraphicsStatus> OVERRIDE_GRAPHICS_MODE = register(
            "override_graphics_mode",
            NoxesiumExtraCodecs.forEnum(GraphicsStatus.class),
            NoxesiumExtraStreamCodecs.forEnum(GraphicsStatus.class));

    /**
     * If set, enables a custom smoother riptide trident implementation. Requires server-side adjustments.
     */
    public static NoxesiumComponentType<Unit> ENABLE_SMOOTHER_CLIENT_TRIDENT =
            register("enable_smoother_client_trident", Unit.CODEC, Unit.STREAM_CODEC);

    /**
     * Sets the amount of ticks the riptide has coyote time for.
     */
    public static NoxesiumComponentType<Integer> RIPTIDE_COYOTE_TIME =
            register("riptide_coyote_time", Codec.INT, ByteBufCodecs.INT);

    /**
     * If set, enables the ability to pre-charge riptide tridents.
     */
    public static NoxesiumComponentType<Unit> RIPTIDE_PRE_CHARGING =
            register("riptide_pre_charging", Unit.CODEC, Unit.STREAM_CODEC);

    /**
     * Restricts available debug options available to the player.
     */
    public static NoxesiumComponentType<List<Integer>> RESTRICT_DEBUG_OPTIONS = register(
            "restrict_debug_options",
            NoxesiumExtraCodecs.INT_LIST,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.VAR_INT));

    /**
     * Allows tripwires and note blocks to have server authoritative updates. This means the client does not
     * attempt to make any local block state changes for these blocks.
     */
    public static NoxesiumComponentType<Unit> SERVER_AUTHORITATIVE_BLOCK_UPDATES =
            register("server_authoritative_block_updates", Unit.CODEC, Unit.STREAM_CODEC);

    /**
     * Registers a new component type to the registry.
     */
    private static <T> NoxesiumComponentType<T> register(
            String key, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return NoxesiumRegistries.GAME_COMPONENTS.register(
                Key.key(NoxesiumReferences.NAMESPACE, key),
                new NoxesiumComponentType<T>(
                        NoxesiumReferences.NAMESPACE,
                        key,
                        codec,
                        streamCodec,
                        new NoxesiumComponentListener<T, Minecraft>()));
    }
}
