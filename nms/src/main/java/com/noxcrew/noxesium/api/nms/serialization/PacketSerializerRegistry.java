package com.noxcrew.noxesium.api.nms.serialization;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

/**
 * Stores the serializers for different packets.
 */
public class PacketSerializerRegistry {
    private static final Map<NoxesiumPayloadType<?>, StreamCodec<? super RegistryFriendlyByteBuf, ?>> serializers =
            new HashMap<>();

    /**
     * Returns the serializers to use for the given component in the given registry.
     */
    @Nullable
    public static <T extends NoxesiumPacket> StreamCodec<RegistryFriendlyByteBuf, T> getSerializers(
            NoxesiumPayloadType<T> payloadType) {
        return (StreamCodec<RegistryFriendlyByteBuf, T>) serializers.get(payloadType);
    }

    /**
     * Registers a new serializer for the given packet type.
     */
    public static <T extends NoxesiumPacket> void registerSerializer(
            NoxesiumPayloadType<T> payloadType, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        serializers.put(payloadType, streamCodec);
    }
}
