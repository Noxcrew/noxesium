package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.feature.sounds.EntityNoxesiumSoundInstance;
import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundInstance;
import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundModule;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * Sent by a server to start a Noxesium custom sound. If a sound with the same id
 * is already playing, that sound will be stopped.
 */
public class ClientboundCustomSoundStartPacket extends ClientboundNoxesiumPacket {

    public final int id;
    public final ResourceLocation sound;
    public final SoundSource source;

    /**
     * The position where the sound is playing, can be null.
     */
    public final Vec3 position;

    /**
     * The entity that the sound is playing relative to, can be null.
     */
    public final Integer entityId;

    public final boolean looping;

    /**
     * Whether this sound has attenuation. If `false`, the sound is played at the same
     * volume regardless of distance to the position. Should be `true` for most sounds.
     */
    public final boolean attenuation;
    /**
     * Whether to ignore playing the sound if the id is already playing
     * another sound.
     */
    public final boolean ignoreIfPlaying;
    public final float volume;
    public final float pitch;

    /**
     * The offset of the sound in seconds.
     */
    public final float offset;

    public ClientboundCustomSoundStartPacket(FriendlyByteBuf buf) {
        super(buf.readVarInt());
        this.id = buf.readVarInt();
        this.sound = buf.readResourceLocation();
        this.source = buf.readEnum(SoundSource.class);
        this.looping = buf.readBoolean();
        this.attenuation = buf.readBoolean();
        this.ignoreIfPlaying = buf.readBoolean();
        this.volume = buf.readFloat();
        this.pitch = buf.readFloat();

        var mode = buf.readVarInt();
        if (mode == 0) {
            this.position = buf.readVec3();
            this.entityId = null;
        } else if (mode == 1) {
            this.position = null;
            this.entityId = buf.readVarInt();
        } else {
            this.position = null;
            this.entityId = null;
        }

        if (buf.readBoolean()) {
            var unix = buf.readLong();
            var passed = System.currentTimeMillis() - unix;
            this.offset = Math.max(0, passed) / 1000f;
        } else {
            this.offset = buf.readFloat();
        }
    }

    @Override
    public void receive(LocalPlayer player, PacketSender responseSender) {
        var manager = NoxesiumSoundModule.getInstance();

        // Determine the sound instance to play
        NoxesiumSoundInstance sound = null;
        if (position != null) {
            sound = new NoxesiumSoundInstance(this.sound, this.source, this.position, this.volume, this.pitch, this.looping, this.attenuation, this.offset);
        } else if (entityId != null) {
            var entity = player.connection.getLevel().getEntity(this.entityId);
            if (entity != null) {
                sound = new EntityNoxesiumSoundInstance(this.sound, this.source, entity, this.volume, this.pitch, this.looping, this.attenuation, this.offset);
            }
        }
        if (sound == null) {
            sound = new EntityNoxesiumSoundInstance(this.sound, this.source, player, this.volume, this.pitch, this.looping, this.attenuation, this.offset);
        }
        manager.play(id, sound, this.ignoreIfPlaying);
    }

    @Override
    public PacketType<?> getType() {
        return NoxesiumPackets.CLIENT_START_SOUND;
    }
}
