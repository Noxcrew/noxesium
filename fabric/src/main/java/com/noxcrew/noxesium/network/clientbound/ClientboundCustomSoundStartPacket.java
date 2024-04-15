package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.network.NoxesiumPacket;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.NoxesiumPayloadType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * Sent by a server to start a Noxesium custom sound. If a sound with the same id
 * is already playing, that sound will be stopped.
 *
 * @param position        The position where the sound is playing, can be null.
 * @param entityId        The entity that the sound is playing relative to, can be null.
 * @param attenuation     Whether this sound has attenuation. If `false`, the sound is played at the same
 *                        volume regardless of distance to the position. Should be `true` for most sounds.
 * @param ignoreIfPlaying Whether to ignore playing the sound if the id is already playing another sound.
 * @param offset          The offset of the sound in seconds.
 */
public record ClientboundCustomSoundStartPacket(
        int id,
        ResourceLocation sound,
        SoundSource source,
        boolean looping,
        boolean attenuation,
        boolean ignoreIfPlaying,
        float volume,
        float pitch,
        Vec3 position,
        Integer entityId,
        Long unix,
        Float offset
) implements NoxesiumPacket {
    public static final StreamCodec<FriendlyByteBuf, ClientboundCustomSoundStartPacket> STREAM_CODEC = CustomPacketPayload.codec(ClientboundCustomSoundStartPacket::write, ClientboundCustomSoundStartPacket::new);
    public static final NoxesiumPayloadType<ClientboundCustomSoundStartPacket> TYPE = NoxesiumPackets.client("start_sound", STREAM_CODEC);

    private ClientboundCustomSoundStartPacket(FriendlyByteBuf buf) {
        this(
                buf,
                buf.readVarInt(),
                buf.readResourceLocation(),
                buf.readEnum(SoundSource.class),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readVarInt()
        );
    }

    private ClientboundCustomSoundStartPacket(
            FriendlyByteBuf buf,
            int id,
            ResourceLocation sound,
            SoundSource source,
            boolean looping,
            boolean attenuation,
            boolean ignoreIfPlaying,
            float volume,
            float pitch,
            int mode
    ) {
        this(
                buf,
                id,
                sound,
                source,
                looping,
                attenuation,
                ignoreIfPlaying,
                volume,
                pitch,
                mode == 0 ? buf.readVec3() : null,
                mode == 1 ? buf.readVarInt() : null,
                buf.readBoolean()
        );
    }

    private ClientboundCustomSoundStartPacket(
            FriendlyByteBuf buf,
            int id,
            ResourceLocation sound,
            SoundSource source,
            boolean looping,
            boolean attenuation,
            boolean ignoreIfPlaying,
            float volume,
            float pitch,
            Vec3 position,
            Integer entityId,
            boolean unix
    ) {
        this(
                id,
                sound,
                source,
                looping,
                attenuation,
                ignoreIfPlaying,
                volume,
                pitch,
                position,
                entityId,
                unix ? buf.readLong() : null,
                !unix ? buf.readFloat() : null
        );
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(id);
        buf.writeResourceLocation(sound);
        buf.writeEnum(source);
        buf.writeBoolean(looping);
        buf.writeBoolean(attenuation);
        buf.writeBoolean(ignoreIfPlaying);
        buf.writeFloat(volume);
        buf.writeFloat(pitch);

        if (position != null) {
            buf.writeVarInt(0);
            buf.writeVec3(position);
        } else if (entityId != null) {
            buf.writeVarInt(1);
            buf.writeVarInt(entityId);
        } else {
            buf.writeVarInt(2);
        }
        if (unix != null) {
            buf.writeBoolean(true);
            buf.writeLong(unix);
        } else {
            buf.writeBoolean(false);
            buf.writeFloat(offset);
        }
    }

    /**
     * Determines the offset to start the sound at. This can be defined through either a unix
     * timestamp or an offset value.
     */
    public float determineOffset() {
        return unix != null ? Math.max(0, System.currentTimeMillis() - unix) / 1000f : offset;
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return TYPE;
    }
}