package com.noxcrew.noxesium.fabric.registry;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.fabric.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumExtraCodecs;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumExtraStreamCodecs;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumRegistries;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.key.Key;
import net.minecraft.client.GraphicsStatus;
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
            register("disable_spin_attack_collisions", Unit.CODEC, Unit.STREAM_CODEC); // default: not present

    /**
     * An integer pixel amount to vertically offset the HUD held item name.
     * Useful for avoiding overlapping action bar icons. Positive values move the text up.
     */
    public static NoxesiumComponentType<Integer> HELD_ITEM_NAME_OFFSET =
            register("held_item_name_offset", Codec.INT, ByteBufCodecs.INT); // default: 0

    /**
     * If set, the client player is prevented from using any mouse inputs to
     * move their camera.
     */
    public static NoxesiumComponentType<Unit> CAMERA_LOCKED =
            register("camera_locked", Unit.CODEC, Unit.STREAM_CODEC); // default: not present
    /*
       On change trigger:

       // Using comparison here to avoid unboxing since the booleans are nullable
       if (Objects.equals(oldValue, true) && !Objects.equals(newValue, true)) {
           // Remove all accumulated mouse movement whenever the camera stops being locked
           var mouseHandler = (MouseHandlerExt) Minecraft.getInstance().mouseHandler;
           mouseHandler.setAccumulatedDeltaX(0.0);
           mouseHandler.setAccumulatedDeltaY(0.0);
       }
    */

    /**
     * If set, vanilla background music is fully disabled.
     */
    public static NoxesiumComponentType<Unit> DISABLE_VANILLA_MUSIC =
            register("disable_vanilla_music", Unit.CODEC, Unit.STREAM_CODEC); // default: not present
    /*
       On change trigger:

       // If the sound options screen is open we need to close it as it may have changed
       var minecraft = Minecraft.getInstance();
       if (minecraft.screen instanceof SoundOptionsScreen) {
           minecraft.screen.onClose();
       }

       // If background music is playing and we've just enabled custom music, stop it!
       // This prevents all vanilla music from playing on servers with custom music.
       if (Objects.equals(newValue, true)) {
           minecraft.getMusicManager().stopPlaying();
       }
    */

    /**
     * If set, disables boat collision on the client side, useful for movement games involving
     * boats and other entities in one area. Similar mechanism must exist server side to prevent
     * lag-backs with this enabled.
     */
    public static NoxesiumComponentType<Unit> DISABLE_BOAT_COLLISIONS =
            register("disable_boat_collisions", Unit.CODEC, Unit.STREAM_CODEC); // default: not present

    /**
     * Allows overriding the item that is used when resolving the capabilities of an empty item slot.
     */
    public static NoxesiumComponentType<ItemStack> HAND_ITEM_OVERRIDE =
            register("hand_item_override", ItemStack.OPTIONAL_CODEC, ItemStack.OPTIONAL_STREAM_CODEC); // default: empty

    /**
     * If set, forces the client to run chunk updates immediately instead of deferring
     * them to the off-thread. Can be used to force a client to update the world
     * to avoid de-synchronizations on chunk updates.
     */
    public static NoxesiumComponentType<Unit> DISABLE_DEFERRED_CHUNK_UPDATES =
            register("disable_deferred_chunk_updates", Unit.CODEC, Unit.STREAM_CODEC); // default: not present

    /**
     * Defines a list of items to show in a custom creative tab.
     */
    public static NoxesiumComponentType<List<ItemStack>> CUSTOM_CREATIVE_ITEMS = register(
            "custom_creative_items",
            Codec.list(ItemStack.OPTIONAL_CODEC),
            ByteBufCodecs.collection(ArrayList::new, ItemStack.OPTIONAL_STREAM_CODEC)); // default: empty list

    /**
     * Moves the handheld map to be shown in the top left/right corner instead of
     * in the regular hand slot when set to true or fully hides when set to false.
     * Map follows client setting if value is not set.
     */
    public static NoxesiumComponentType<Boolean> SHOW_MAP_IN_UI =
            register("show_map_in_ui", Codec.BOOL, ByteBufCodecs.BOOL); // default: not present

    /**
     * Allows the server to override the graphics mode used by the client.
     */
    public static NoxesiumComponentType<GraphicsStatus> OVERRIDE_GRAPHICS_MODE = register(
            "override_graphics_mode",
            NoxesiumExtraCodecs.forEnum(GraphicsStatus.class),
            NoxesiumExtraStreamCodecs.forEnum(GraphicsStatus.class)); // default: not present
    /*
       On change listener:

       // We need to call this whenever we change the display type.
       if (Minecraft.getInstance().levelRenderer != null) {
           Minecraft.getInstance().levelRenderer.allChanged();
       }
    */

    /**
     * If set, enables a custom smoother riptide trident implementation. Requires server-side adjustments.
     */
    public static NoxesiumComponentType<Unit> ENABLE_SMOOTHER_CLIENT_TRIDENT =
            register("enable_smoother_client_trident", Unit.CODEC, Unit.STREAM_CODEC); // default: not present

    /**
     * Sets the amount of ticks the riptide has coyote time for.
     */
    public static NoxesiumComponentType<Integer> RIPTIDE_COYOTE_TIME =
            register("riptide_coyote_time", Codec.INT, ByteBufCodecs.INT); // default: 5

    /**
     * If set, enables the ability to pre-charge riptide tridents.
     */
    public static NoxesiumComponentType<Unit> RIPTIDE_PRE_CHARGING =
            register("riptide_pre_charging", Unit.CODEC, Unit.STREAM_CODEC); // default: not present

    /**
     * Restricts available debug options available to the player.
     */
    public static NoxesiumComponentType<List<Integer>> RESTRICT_DEBUG_OPTIONS = register(
            "restrict_debug_options",
            NoxesiumExtraCodecs.INT_LIST,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.VAR_INT)); // default: empty list
    /*
       On change listener:

       if (Minecraft.getInstance().options != null) {
           Minecraft.getInstance().options.save();
       }
       // We need to call this when hitboxes & chunk boundaries are updated.
       if (Minecraft.getInstance().levelRenderer != null) {
           Minecraft.getInstance().levelRenderer.allChanged();
       }
    */

    /**
     * Registers a new component type to the registry.
     */
    private static <T> NoxesiumComponentType<T> register(
            String key, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return NoxesiumRegistries.GAME_COMPONENTS.register(
                Key.key(NoxesiumReferences.NAMESPACE, key),
                new NoxesiumComponentType<T>(NoxesiumReferences.NAMESPACE, key, codec, streamCodec));
    }
}
