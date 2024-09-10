package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.network.NoxesiumPacket;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.NoxesiumPayloadType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent by a server to change the volume of a sound. The interpolation time can be
 * used to fade the sound up or down over an amount of ticks.
 *
 * @param startVolume An optional volume to start the interpolation from. If absent the current volume of the sound is used instead.
 */
public record ClientboundCustomSoundModifyPacket(int id, float volume, int interpolationTicks, Float startVolume) implements NoxesiumPacket {
    public static final StreamCodec<FriendlyByteBuf, ClientboundCustomSoundModifyPacket> STREAM_CODEC = CustomPacketPayload.codec(ClientboundCustomSoundModifyPacket::write, ClientboundCustomSoundModifyPacket::new);

    private ClientboundCustomSoundModifyPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readFloat(), buf.readVarInt(), buf.readBoolean() ? buf.readFloat() : null);
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(id);
        buf.writeFloat(volume);
        buf.writeVarInt(interpolationTicks);
        if (startVolume == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeFloat(startVolume);
        }
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return NoxesiumPackets.CLIENT_CUSTOM_SOUND_MODIFY;
    }
}