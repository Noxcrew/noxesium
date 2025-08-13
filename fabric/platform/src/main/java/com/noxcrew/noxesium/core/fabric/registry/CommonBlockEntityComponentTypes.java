package com.noxcrew.noxesium.core.fabric.registry;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.RegistryCollection;

/**
 * Stores all Noxesium block entity component types.
 */
public class CommonBlockEntityComponentTypes {
    public static final RegistryCollection<NoxesiumComponentType<?>> INSTANCE =
            new RegistryCollection<>(NoxesiumRegistries.BLOCK_ENTITY_COMPONENTS);

    /**
     * Sets the default height of a beacon beam.
     */
    public static NoxesiumComponentType<Integer> BEACON_BEAM_HEIGHT = register("beacon_beam_height", Codec.INT);

    /**
     * Registers a new component type to the registry.
     */
    private static <T> NoxesiumComponentType<T> register(String key, Codec<T> codec) {
        return NoxesiumRegistries.register(INSTANCE, NoxesiumReferences.NAMESPACE, key, codec, null, null);
    }
}
