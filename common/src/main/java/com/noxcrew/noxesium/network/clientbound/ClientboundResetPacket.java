package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.network.NoxesiumPacket;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.NoxesiumPayloadType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent by the server to reset one or more features of the client.
 * The flags byte has the following results:
 * 0x01 - Resets all server rule values
 * 0x02 - Unused
 */
public record ClientboundResetPacket(byte flags) implements NoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundResetPacket> STREAM_CODEC =
            CustomPacketPayload.codec(ClientboundResetPacket::write, ClientboundResetPacket::new);

    private ClientboundResetPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readByte());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeByte(flags);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return NoxesiumPackets.CLIENT_RESET;
    }
}
