package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.network.NoxesiumPacket;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.NoxesiumPayloadType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent by a server to stop a custom Noxesium sound by its id.
 */
public record ClientboundCustomSoundStopPacket(int id) implements NoxesiumPacket {
    public static final StreamCodec<FriendlyByteBuf, ClientboundCustomSoundStopPacket> STREAM_CODEC = CustomPacketPayload.codec(ClientboundCustomSoundStopPacket::write, ClientboundCustomSoundStopPacket::new);
    public static final NoxesiumPayloadType<ClientboundCustomSoundStopPacket> TYPE = NoxesiumPackets.client("stop_sound", STREAM_CODEC);

    private ClientboundCustomSoundStopPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(id);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return TYPE;
    }
}