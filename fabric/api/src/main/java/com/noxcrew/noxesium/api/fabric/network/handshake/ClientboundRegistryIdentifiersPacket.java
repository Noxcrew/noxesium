package com.noxcrew.noxesium.api.fabric.network.handshake;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent to the client after handshaking to populate a registry using client-known identifiers.
 */
public record ClientboundRegistryIdentifiersPacket(ResourceLocation registry, Map<Integer, ResourceLocation> ids)
        implements NoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRegistryIdentifiersPacket> STREAM_CODEC =
            CustomPacketPayload.codec(
                    ClientboundRegistryIdentifiersPacket::write, ClientboundRegistryIdentifiersPacket::new);

    private ClientboundRegistryIdentifiersPacket(RegistryFriendlyByteBuf buf) {
        this(
                buf.readResourceLocation(),
                buf.readMap(FriendlyByteBuf::readVarInt, FriendlyByteBuf::readResourceLocation));
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(registry);
        buf.writeMap(ids, FriendlyByteBuf::writeVarInt, FriendlyByteBuf::writeResourceLocation);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return HandshakePackets.INSTANCE.CLIENTBOUND_REGISTRY_IDS;
    }
}
