package com.noxcrew.noxesium.core.nms.registry;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.component.NoxesiumComponentListener;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.nms.codec.NoxesiumCodecs;
import com.noxcrew.noxesium.api.nms.codec.NoxesiumStreamCodecs;
import com.noxcrew.noxesium.api.nms.registry.ComponentSerializerRegistry;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.util.GraphicsMode;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * Registers the serializers for different game components.
 */
public class CommonGameComponentSerializers {
    /**
     * Registers all serializers.
     */
    public static void register() {
        register(
                CommonGameComponentTypes.DISABLE_SPIN_ATTACK_COLLISIONS,
                NoxesiumCodecs.UNIT,
                NoxesiumStreamCodecs.UNIT);
        register(CommonGameComponentTypes.HELD_ITEM_NAME_OFFSET, Codec.INT, ByteBufCodecs.VAR_INT);
        register(CommonGameComponentTypes.CAMERA_LOCKED, NoxesiumCodecs.UNIT, NoxesiumStreamCodecs.UNIT);
        register(CommonGameComponentTypes.DISABLE_VANILLA_MUSIC, NoxesiumCodecs.UNIT, NoxesiumStreamCodecs.UNIT);
        register(CommonGameComponentTypes.DISABLE_BOAT_COLLISIONS, NoxesiumCodecs.UNIT, NoxesiumStreamCodecs.UNIT);
        register(
                CommonGameComponentTypes.DISABLE_DEFERRED_CHUNK_UPDATES,
                NoxesiumCodecs.UNIT,
                NoxesiumStreamCodecs.UNIT);
        register(CommonGameComponentTypes.SHOW_MAP_IN_UI, Codec.BOOL, ByteBufCodecs.BOOL);
        register(
                CommonGameComponentTypes.OVERRIDE_GRAPHICS_MODE,
                NoxesiumCodecs.forEnum(GraphicsMode.class),
                NoxesiumStreamCodecs.forEnum(GraphicsMode.class));
        register(
                CommonGameComponentTypes.ENABLE_SMOOTHER_CLIENT_TRIDENT,
                NoxesiumCodecs.UNIT,
                NoxesiumStreamCodecs.UNIT);
        register(CommonGameComponentTypes.RIPTIDE_COYOTE_TIME, Codec.INT, ByteBufCodecs.VAR_INT);
        register(CommonGameComponentTypes.RIPTIDE_PRE_CHARGING, NoxesiumCodecs.UNIT, NoxesiumStreamCodecs.UNIT);
        register(
                CommonGameComponentTypes.RESTRICT_DEBUG_OPTIONS,
                NoxesiumCodecs.INT_LIST,
                ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.VAR_INT));
        register(
                CommonGameComponentTypes.SERVER_AUTHORITATIVE_BLOCK_UPDATES,
                NoxesiumCodecs.UNIT,
                NoxesiumStreamCodecs.UNIT);
        register(CommonGameComponentTypes.REPORT_MOUSE_CLICKS, NoxesiumCodecs.UNIT, NoxesiumStreamCodecs.UNIT);

        register(NmsGameComponentTypes.HAND_ITEM_OVERRIDE, ItemStack.CODEC, ItemStack.STREAM_CODEC);
        register(
                NmsGameComponentTypes.CUSTOM_CREATIVE_ITEMS,
                Codec.list(ItemStack.OPTIONAL_CODEC),
                ByteBufCodecs.collection(ArrayList::new, ItemStack.OPTIONAL_STREAM_CODEC));
    }

    /**
     * Registers a new serializer to the registry.
     */
    private static <T> void register(
            NoxesiumComponentType<T> component,
            Codec<T> codec,
            StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        ComponentSerializerRegistry.registerSerializers(
                NoxesiumRegistries.GAME_COMPONENTS,
                component,
                codec,
                streamCodec,
                new NoxesiumComponentListener<T, Minecraft>());
    }
}
