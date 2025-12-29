package com.noxcrew.noxesium.core.nms.serialization;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.nms.codec.NoxesiumCodecs;
import com.noxcrew.noxesium.api.nms.serialization.ComponentSerializerRegistry;
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
        register(CommonItemComponentTypes.QIB_BEHAVIOR, NoxesiumCodecs.KEY);
    }

    /**
     * Registers a new serializer to the registry.
     */
    private static <T> void register(NoxesiumComponentType<T> component, Codec<T> codec) {
        ComponentSerializerRegistry.registerSerializers(
                NoxesiumRegistries.ITEM_COMPONENTS, component, codec, null, null);
    }
}
