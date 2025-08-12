package com.noxcrew.noxesium.fabric.network.clientbound;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.fabric.network.CommonPackets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent to the client when the server is first informed of it existing, this contains information
 * about what protocol version the server supports.
 */
public record ClientboundServerInformationPacket(int maxProtocolVersion) implements NoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundServerInformationPacket> STREAM_CODEC =
            CustomPacketPayload.codec(
                    ClientboundServerInformationPacket::write, ClientboundServerInformationPacket::new);

    private ClientboundServerInformationPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(maxProtocolVersion);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return CommonPackets.INSTANCE.CLIENT_SERVER_INFO;
    }
}
