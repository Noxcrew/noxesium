package com.noxcrew.noxesium.core.fabric.network;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.ConnectionProtocolType;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.network.json.JsonSerializedPacket;
import com.noxcrew.noxesium.api.network.json.JsonSerializerRegistry;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadGroup;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType;
import com.noxcrew.noxesium.api.nms.serialization.PacketSerializerRegistry;
import com.noxcrew.noxesium.core.fabric.mixin.PayloadTypeRegistryExt;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.kyori.adventure.key.Key;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Extends the Noxesium payload type with fabric specific networking code.
 */
public class FabricPayloadType<T extends NoxesiumPacket> extends NoxesiumPayloadType<T> {
    /**
     * The internal type of this payload.
     */
    public final CustomPacketPayload.Type<NoxesiumPayload<T>> type;

    public FabricPayloadType(NoxesiumPayloadGroup group, Key id, Class<T> clazz, boolean clientToServer) {
        super(group, id, clazz, clientToServer);
        this.type = new CustomPacketPayload.Type<>(ResourceLocation.parse(id.asString()));
    }

    @Override
    public void register(@Nullable NoxesiumEntrypoint entrypoint) {
        // Create a custom payload that uses the payload object as a wrapper so we can
        // provide a custom stream codec to use for this packet.
        if (clientToServer) {
            PayloadTypeRegistry.playC2S().register(type, getStreamCodec());
        } else {
            PayloadTypeRegistry.playS2C().register(type, getStreamCodec());
        }

        // Register and bind after adding to the registry!
        super.register(entrypoint);
    }

    @Override
    public void unregister() {
        // Unbind before removing from the registry!
        super.unregister();

        if (clientToServer) {
            unregisterPacket(PayloadTypeRegistry.playC2S(), type.id());
        } else {
            unregisterPacket(PayloadTypeRegistry.playS2C(), type.id());
        }
    }

    @Override
    public void bind(ConnectionProtocolType protocolType) {
        super.bind(protocolType);
        if (!clientToServer) {
            ClientPlayNetworking.registerReceiver(type, new FabricPacketHandler<>());
        }
    }

    @Override
    public void unbind(ConnectionProtocolType protocolType) {
        super.unbind(protocolType);
        if (!clientToServer) {
            ClientPlayNetworking.unregisterReceiver(type.id());
        }
    }

    /**
     * Returns a stream codec for this payload.
     */
    private StreamCodec<RegistryFriendlyByteBuf, NoxesiumPayload<T>> getStreamCodec() {
        var payloadType = this;
        if (payloadType.jsonSerialized) {
            var serializer = JsonSerializerRegistry.getInstance()
                    .getSerializer(payloadType
                            .clazz
                            .getAnnotation(JsonSerializedPacket.class)
                            .value());
            return new StreamCodec<>() {
                @Override
                @NotNull
                public NoxesiumPayload<T> decode(RegistryFriendlyByteBuf buffer) {
                    return new NoxesiumPayload<>(payloadType, serializer.decode(buffer.readUtf(), payloadType.clazz));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, NoxesiumPayload<T> payload) {
                    buffer.writeUtf(serializer.encode(payload.value(), payloadType.clazz));
                }
            };
        }

        var codec = PacketSerializerRegistry.getSerializers(payloadType.typeClass());
        return new StreamCodec<>() {
            @Override
            @NotNull
            public NoxesiumPayload<T> decode(RegistryFriendlyByteBuf buffer) {
                return new NoxesiumPayload<>(payloadType, codec.decode(buffer));
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, NoxesiumPayload<T> payload) {
                codec.encode(buffer, payload.value());
            }
        };
    }

    /**
     * Unregisters the packet with the given id from the given registry.
     */
    private static void unregisterPacket(PayloadTypeRegistry<?> registry, ResourceLocation id) {
        ((PayloadTypeRegistryExt) registry).getPacketTypes().remove(id);
    }
}
