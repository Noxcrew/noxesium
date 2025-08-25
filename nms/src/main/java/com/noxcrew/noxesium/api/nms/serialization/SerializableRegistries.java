package com.noxcrew.noxesium.api.nms.serialization;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

/**
 * Stores the serializers for registries.
 */
public class SerializableRegistries {
    private static final Map<NoxesiumRegistry<?>, SerializerPair<?>> serializers = new HashMap<>();

    /**
     * Returns all serializers.
     */
    public static Map<NoxesiumRegistry<?>, SerializerPair<?>> getAllSerializers() {
        return serializers;
    }

    /**
     * Returns the serializers to use for the given registry;
     */
    @Nullable
    public static <T> SerializerPair<T> getSerializers(NoxesiumRegistry<T> registry) {
        return (SerializerPair<T>) serializers.get(registry);
    }

    /**
     * Registers a new serializer for the given registry.
     */
    public static <T> void registerSerializers(
            NoxesiumRegistry<T> registry,
            Codec<T> codec,
            @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        serializers.put(registry, new SerializerPair<>(codec, streamCodec));
    }
}
