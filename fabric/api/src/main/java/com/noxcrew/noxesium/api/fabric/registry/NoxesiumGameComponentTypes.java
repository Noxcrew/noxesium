package com.noxcrew.noxesium.api.fabric.registry;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.fabric.component.NoxesiumComponentType;
import net.kyori.adventure.key.Key;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Stores all Noxesium game component types.
 */
public class NoxesiumGameComponentTypes {

    /**
     * If `true, disables the riptide spin attack on the trident from colliding with any entities,
     * useful for non-pvp mini-games where the trident is used as a movement tool.
     */
    public static NoxesiumComponentType<Boolean> DISABLE_SPIN_ATTACK_COLLISIONS = register(
            "disable_spin_attack_collisions",
            Codec.BOOL,
            ByteBufCodecs.BOOL
    ); //default: false

    /**
     * An integer pixel amount to vertically offset the HUD held item name.
     * Useful for avoiding overlapping action bar icons. Positive values move the text up.
     */
    public static NoxesiumComponentType<Integer> HELD_ITEM_NAME_OFFSET = register(
            "held_item_name_offset",
            Codec.INT,
            ByteBufCodecs.INT
    ); //default: 0

    /**
     * Registers a new component type to the registry.
     */
    private static <T> NoxesiumComponentType<T> register(String key, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return NoxesiumRegistries.GAME_COMPONENTS.register(
                Key.key(NoxesiumReferences.NAMESPACE, key),
                new NoxesiumComponentType<T>(ResourceLocation.fromNamespaceAndPath(NoxesiumReferences.NAMESPACE, key), codec, streamCodec)
        );
    }
}
