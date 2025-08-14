package com.noxcrew.noxesium.core.fabric.registry;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.fabric.registry.ComponentSerializerRegistry;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumCodecs;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.core.registry.CommonItemComponentTypes;

/**
 * Registers the serializers for different item components.
 */
public class CommonItemComponentSerializers {
    /**
     * Registers all serializers.
     */
    public static void register() {
        register(CommonItemComponentTypes.IMMOVABLE, NoxesiumCodecs.UNIT);
        register(CommonItemComponentTypes.HOVER_SOUND, NoxesiumCodecs.HOVER_SOUND);
        register(CommonItemComponentTypes.HOVERABLE, NoxesiumCodecs.HOVERABLE);
    }

    /**
     * Registers a new serializer to the registry.
     */
    private static <T> void register(NoxesiumComponentType<T> component, Codec<T> codec) {
        ComponentSerializerRegistry.registerSerializers(
                NoxesiumRegistries.ITEM_COMPONENTS, component, codec, null, null);
    }
}
