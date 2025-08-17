package com.noxcrew.noxesium.api.nms.serialization;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.component.NoxesiumComponentListener;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

/**
 * Stores the serializers for different components.
 */
public class ComponentSerializerRegistry {
    private static final Map<
                    NoxesiumRegistry<NoxesiumComponentType<?>>, Map<NoxesiumComponentType<?>, ComponentSerializers<?>>>
            serializers = new HashMap<>();

    /**
     * Returns the serializers to use for the given component in the given registry.
     */
    @Nullable
    public static <T> ComponentSerializers<T> getSerializers(
            NoxesiumRegistry<NoxesiumComponentType<?>> registry, NoxesiumComponentType<T> component) {
        var registryEntries = serializers.get(registry);
        if (registryEntries != null) {
            return (ComponentSerializers<T>) registryEntries.get(component);
        }
        return null;
    }

    /**
     * Registers a new serializer for the given component in the given registry.
     */
    public static <T> void registerSerializers(
            NoxesiumRegistry<NoxesiumComponentType<?>> registry,
            NoxesiumComponentType<T> component,
            Codec<T> codec,
            @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec,
            @Nullable NoxesiumComponentListener<T, ?> listener) {
        serializers
                .computeIfAbsent(registry, (ignored) -> new HashMap<>())
                .put(component, new ComponentSerializers<T>(codec, streamCodec, listener));
    }
}
