package com.noxcrew.noxesium.api.nms.serialization;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores the serializers for different packets.
 */
public class PacketSerializerRegistry {
    private static final Map<NoxesiumPayloadType<?>, StreamCodec<? super RegistryFriendlyByteBuf, ?>> serializers =
            new HashMap<>();

    /**
     * Returns the serializers to use for the given component in the given registry.
     */
    @NotNull
    public static <T extends NoxesiumPacket> StreamCodec<RegistryFriendlyByteBuf, T> getSerializers(
            NoxesiumPayloadType<T> payloadType) {
        if (!serializers.containsKey(payloadType)) {
            throw new IllegalArgumentException("No serializer defined for packet type '" + payloadType.id() + "'");
        }
        return (StreamCodec<RegistryFriendlyByteBuf, T>) serializers.get(payloadType);
    }

    /**
     * Registers a new serializer for the given packet type.
     */
    public static <T extends NoxesiumPacket> void registerSerializer(
            NoxesiumPayloadType<T> payloadType, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        Preconditions.checkState(!payloadType.jsonSerialized, "Cannot register a serializer for a JSON serialized packet");
        serializers.put(payloadType, streamCodec);
    }
}
