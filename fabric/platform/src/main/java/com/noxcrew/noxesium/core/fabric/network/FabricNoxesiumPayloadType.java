package com.noxcrew.noxesium.core.fabric.network;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.nms.network.payload.NoxesiumPayloadType;
import com.noxcrew.noxesium.core.fabric.mixin.PayloadTypeRegistryExt;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Extends the Noxesium payload type with fabric specific networking code.
 */
public class FabricNoxesiumPayloadType<T extends NoxesiumPacket> extends NoxesiumPayloadType<T> {

    public FabricNoxesiumPayloadType(
            ResourceLocation key,
            StreamCodec<RegistryFriendlyByteBuf, T> codec,
            Class<T> clazz,
            boolean clientToServer) {
        super(key, codec, clazz, clientToServer);
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
            ClientPlayNetworking.registerReceiver(type, new FabricPacketHandler<>());
        }
    }

    @Override
    public void unregister() {
        super.unregister();

        if (clientToServer) {
            unregisterPacket(PayloadTypeRegistry.playC2S(), type.id());
        } else {
            unregisterPacket(PayloadTypeRegistry.playS2C(), type.id());
            ClientPlayNetworking.unregisterReceiver(type.id());
        }
    }

    /**
     * Unregisters the packet with the given id from the given registry.
     */
    private static void unregisterPacket(PayloadTypeRegistry<RegistryFriendlyByteBuf> registry, ResourceLocation id) {
        ((PayloadTypeRegistryExt) registry).getPacketTypes().remove(id);
    }
}
