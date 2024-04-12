package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.network.NoxesiumPacket;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.payload.NoxesiumPayloadType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent to the client when the server is first informed of it existing, this contains information
 * about what protocol version the server supports.
 */
public record ClientboundServerInformationPacket(int maxProtocolVersion) implements NoxesiumPacket {
    public static final StreamCodec<FriendlyByteBuf, ClientboundServerInformationPacket> STREAM_CODEC = CustomPacketPayload.codec(ClientboundServerInformationPacket::write, ClientboundServerInformationPacket::new);
    public static final NoxesiumPayloadType<ClientboundServerInformationPacket> TYPE = NoxesiumPackets.client("server_info", STREAM_CODEC);

    private ClientboundServerInformationPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(maxProtocolVersion);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return TYPE;
    }
}
