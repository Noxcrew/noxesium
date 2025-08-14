package com.noxcrew.noxesium.core.fabric.registry;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.fabric.registry.ComponentSerializerRegistry;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.core.registry.CommonBlockEntityComponentTypes;

/**
 * Registers the serializers for different block entity components.
 */
public class CommonBlockEntityComponentSerializers {
    /**
     * Registers all serializers.
     */
    public static void register() {
        register(CommonBlockEntityComponentTypes.BEACON_BEAM_HEIGHT, Codec.INT);
    }

    /**
     * Registers a new serializer to the registry.
     */
    private static <T> void register(NoxesiumComponentType<T> component, Codec<T> codec) {
        ComponentSerializerRegistry.registerSerializers(
                NoxesiumRegistries.BLOCK_ENTITY_COMPONENTS, component, codec, null, null);
    }
}
