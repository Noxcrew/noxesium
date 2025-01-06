package com.noxcrew.noxesium.network.serverbound;

import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.NoxesiumPayloadType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent to the server when the client first joins to establish the version of the
 * client being used.
 */
public record ServerboundClientInformationPacket(int protocolVersion, String versionString)
        implements ServerboundNoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundClientInformationPacket> STREAM_CODEC =
            CustomPacketPayload.codec(
                    ServerboundClientInformationPacket::write, ServerboundClientInformationPacket::new);

    private ServerboundClientInformationPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), buf.readUtf());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeByte(protocolVersion);
        buf.writeUtf(versionString);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return NoxesiumPackets.SERVER_CLIENT_INFO;
    }
}
