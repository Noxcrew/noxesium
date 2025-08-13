package com.noxcrew.noxesium.fabric.registry;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.fabric.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumRegistries;
import net.kyori.adventure.key.Key;

/**
 * Stores all Noxesium block entity component types.
 */
public class CommonBlockEntityComponentTypes {

    /**
     * Sets the default height of a beacon beam.
     */
    public static NoxesiumComponentType<Integer> BEACON_BEAM_HEIGHT =
            register("beacon_beam_height", Codec.INT); // default: 2048

    /**
     * Registers a new component type to the registry.
     */
    private static <T> NoxesiumComponentType<T> register(String key, Codec<T> codec) {
        return NoxesiumRegistries.BLOCK_ENTITY_COMPONENTS.register(
                Key.key(NoxesiumReferences.NAMESPACE, key),
                new NoxesiumComponentType<T>(NoxesiumReferences.NAMESPACE, key, codec, null));
    }
}
