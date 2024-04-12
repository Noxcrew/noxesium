package com.noxcrew.noxesium.network.serverbound;

import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.payload.NoxesiumPayloadType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent to the server when the client first joins to establish the version of the
 * client being used.
 */
public record ServerboundClientInformationPacket(int protocolVersion) implements ServerboundNoxesiumPacket {
    public static final StreamCodec<FriendlyByteBuf, ServerboundClientInformationPacket> STREAM_CODEC = CustomPacketPayload.codec(ServerboundClientInformationPacket::write, ServerboundClientInformationPacket::new);
    public static final NoxesiumPayloadType<ServerboundClientInformationPacket> TYPE = NoxesiumPackets.server("client_info", STREAM_CODEC);

    private ServerboundClientInformationPacket(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeByte(protocolVersion);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return TYPE;
    }
}
