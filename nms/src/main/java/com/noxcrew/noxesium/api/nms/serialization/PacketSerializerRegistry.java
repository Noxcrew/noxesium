package com.noxcrew.noxesium.api.nms.serialization;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores the serializers for different packets.
 */
public class PacketSerializerRegistry {
    private static final Map<Class<?>, StreamCodec<? super RegistryFriendlyByteBuf, ?>> serializers = new HashMap<>();

    /**
     * Returns the serializers to use for the given component in the given registry.
     */
    @NotNull
    public static <T extends NoxesiumPacket> StreamCodec<RegistryFriendlyByteBuf, T> getSerializers(
            Class<T> classType) {
        if (!serializers.containsKey(classType)) {
            throw new IllegalArgumentException("No serializer defined for packet class '" + classType + "'");
        }
        return (StreamCodec<RegistryFriendlyByteBuf, T>) serializers.get(classType);
    }

    /**
     * Registers a new serializer for the given packet type that only works during
     * the play phase.
     */
    public static <T extends NoxesiumPacket> void registerSerializer(
            Class<T> classType, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        serializers.put(classType, streamCodec);
    }
}
