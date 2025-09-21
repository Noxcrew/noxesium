package com.noxcrew.noxesium.core.fabric.network;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.ConnectionProtocolType;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.network.json.JsonSerializedPacket;
import com.noxcrew.noxesium.api.network.json.JsonSerializerRegistry;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType;
import com.noxcrew.noxesium.api.nms.serialization.PacketSerializerRegistry;
import com.noxcrew.noxesium.core.fabric.mixin.PayloadTypeRegistryExt;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.kyori.adventure.key.Key;
import net.minecraft.network.FriendlyByteBuf;
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

    public FabricPayloadType(Key id, Class<T> clazz, boolean clientToServer, boolean configPhaseCompatible) {
        super(id, clazz, clientToServer, configPhaseCompatible);
        this.type = new CustomPacketPayload.Type<>(ResourceLocation.parse(id.asString()));
    }

    @Override
    public void register(@Nullable NoxesiumEntrypoint entrypoint) {
        super.register(entrypoint);

        // Create a custom payload that uses the payload object as a wrapper so we can
        // provide a custom stream codec to use for this packet.
        if (clientToServer) {
            PayloadTypeRegistry.playC2S().register(type, getStreamCodec());
        } else {
            PayloadTypeRegistry.playS2C().register(type, getStreamCodec());
        }

        // Also register on the config phase if applicable!
        if (configPhaseCompatible) {
            if (clientToServer) {
                PayloadTypeRegistry.configurationC2S().register(type, getStreamCodec());
            } else {
                PayloadTypeRegistry.configurationS2C().register(type, getStreamCodec());
            }
        }
    }

    @Override
    public void unregister() {
        super.unregister();

        if (clientToServer) {
            unregisterPacket(PayloadTypeRegistry.playC2S(), type.id());
        } else {
            unregisterPacket(PayloadTypeRegistry.playS2C(), type.id());
        }

        // Also unregister on the config phase if applicable!
        if (configPhaseCompatible) {
            if (clientToServer) {
                unregisterPacket(PayloadTypeRegistry.configurationC2S(), type.id());
            } else {
                unregisterPacket(PayloadTypeRegistry.configurationS2C(), type.id());
            }
        }
    }

    @Override
    public void bind(ConnectionProtocolType protocolType) {
        super.bind(protocolType);

        switch (protocolType) {
            case CONFIGURATION -> {
                if (!clientToServer) {
                    ClientConfigurationNetworking.registerReceiver(type, new FabricConfigPacketHandler<>());
                }
            }
            case PLAY -> {
                if (!clientToServer) {
                    ClientPlayNetworking.registerReceiver(type, new FabricPlayPacketHandler<>());
                }
            }
            default -> {}
        }
    }

    @Override
    public void unbind(ConnectionProtocolType protocolType) {
        switch (protocolType) {
            case CONFIGURATION -> {
                if (!clientToServer) {
                    ClientConfigurationNetworking.unregisterReceiver(type.id());
                }
            }
            case PLAY -> {
                if (!clientToServer) {
                    ClientPlayNetworking.unregisterReceiver(type.id());
                }
            }
            default -> {}
        }
    }

    /**
     * Returns a stream codec for this payload.
     */
    private StreamCodec<FriendlyByteBuf, NoxesiumPayload<T>> getStreamCodec() {
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
                public NoxesiumPayload<T> decode(FriendlyByteBuf buffer) {
                    return new NoxesiumPayload<>(payloadType, serializer.decode(buffer.readUtf(), payloadType.clazz));
                }

                @Override
                public void encode(FriendlyByteBuf buffer, NoxesiumPayload<T> payload) {
                    buffer.writeUtf(serializer.encode(payload.value(), payloadType.clazz));
                }
            };
        }

        var codec = PacketSerializerRegistry.getSerializers(payloadType);
        return new StreamCodec<>() {
            @Override
            @NotNull
            public NoxesiumPayload<T> decode(FriendlyByteBuf buffer) {
                if (configPhaseCompatible) {
                    return new NoxesiumPayload<>(
                            payloadType,
                            codec.decode(
                                    buffer instanceof RegistryFriendlyByteBuf
                                            ? (RegistryFriendlyByteBuf) buffer
                                            : new RegistryFriendlyByteBuf(buffer, null)));
                } else {
                    Preconditions.checkState(
                            buffer instanceof RegistryFriendlyByteBuf,
                            "Tried to deserialize non-config phase compatible packet " + id());
                    return new NoxesiumPayload<>(payloadType, codec.decode((RegistryFriendlyByteBuf) buffer));
                }
            }

            @Override
            public void encode(FriendlyByteBuf buffer, NoxesiumPayload<T> payload) {
                if (configPhaseCompatible) {
                    codec.encode(
                            buffer instanceof RegistryFriendlyByteBuf
                                    ? (RegistryFriendlyByteBuf) buffer
                                    : new RegistryFriendlyByteBuf(buffer, null),
                            payload.value());
                } else {
                    Preconditions.checkState(
                            buffer instanceof RegistryFriendlyByteBuf,
                            "Tried to serialize non-config phase compatible packet " + id());
                    codec.encode((RegistryFriendlyByteBuf) buffer, payload.value());
                }
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
