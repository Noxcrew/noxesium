package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.network.NoxesiumPacket;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.NoxesiumPayloadType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent by a server to stop a custom Noxesium sound by its id.
 */
public record ClientboundCustomSoundStopPacket(int id) implements NoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCustomSoundStopPacket> STREAM_CODEC =
            CustomPacketPayload.codec(ClientboundCustomSoundStopPacket::write, ClientboundCustomSoundStopPacket::new);

    private ClientboundCustomSoundStopPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(id);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return NoxesiumPackets.CLIENT_CUSTOM_SOUND_STOP;
    }
}
