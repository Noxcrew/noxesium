package com.noxcrew.noxesium.api.nms.serialization;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.registry.ClientNoxesiumRegistry;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry;
import com.noxcrew.noxesium.api.registry.SynchronizedServerNoxesiumRegistry;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

/**
 * Stores the serializers for registries.
 */
public class SerializableRegistries {
    private static final Map<NoxesiumRegistry<?>, CommonSerializerPair<?>> serializers = new HashMap<>();

    /**
     * Returns all serializers.
     */
    public static Map<NoxesiumRegistry<?>, CommonSerializerPair<?>> getAllSerializers() {
        return serializers;
    }

    /**
     * Returns the serializers to use for the given registry;
     */
    @Nullable
    public static <T> CommonSerializerPair<T> getSerializers(NoxesiumRegistry<T> registry) {
        return (CommonSerializerPair<T>) serializers.get(registry);
    }

    /**
     * Registers a new serializer for the given registry.
     */
    public static <T> void registerSerializers(
            NoxesiumRegistry<T> registry,
            Codec<T> codec,
            @Nullable StreamCodec<? super FriendlyByteBuf, T> streamCodec) {
        Preconditions.checkState(registry instanceof SynchronizedServerNoxesiumRegistry
                || registry instanceof ClientNoxesiumRegistry<T>);
        serializers.put(registry, new CommonSerializerPair<>(codec, streamCodec));
    }
}
