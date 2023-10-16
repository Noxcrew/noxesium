package com.noxcrew.noxesium.feature.sounds;

import com.noxcrew.noxesium.mixin.sound.ChannelExt;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

/**
 * The sound instance for custom modifiable sounds
 */
public class NoxesiumSoundInstance extends AbstractTickableSoundInstance {
    public float startOffset;
    private VolumeInterpolation volumeInterpolation;

    public NoxesiumSoundInstance(ResourceLocation sound, SoundSource soundSource, Vec3 position, float volume, float pitch) {
        this(sound, soundSource, position, volume, pitch, false, 0f);
    }
    public NoxesiumSoundInstance(ResourceLocation sound, SoundSource soundSource, Vec3 position, float volume, float pitch, float startOffset) {
        this(sound, soundSource, position, volume, pitch, false, startOffset);
    }
    public NoxesiumSoundInstance(ResourceLocation sound, SoundSource soundSource, Vec3 position, float volume, float pitch, boolean looping, float startOffset) {
        super(SoundEvent.createVariableRangeEvent(sound), soundSource, SoundInstance.createUnseededRandom());
        this.x = position.x();
        this.y = position.y();
        this.z = position.z();
        this.volume = volume;
        this.pitch = pitch;
        this.looping = looping;
        this.startOffset = startOffset;
    }

    @Override
    public void tick() {
        if (volumeInterpolation != null) {
            volumeInterpolation.tick(this);
        }
    }

    /**
     * Sets the volume over a specified time
     * @param volume The new volume to set to
     * @param ticks The fade time
     */
    public void setVolume(float volume, int ticks) {
        if (ticks <= 0) {
            this.volume = volume;
        } else {
            this.volumeInterpolation = new VolumeInterpolation(this.volume, volume, ticks);
        }
    }

    /**
     * A class to manage volume interpolation
     */
    static class VolumeInterpolation {
        int ticks;
        float deltaPerTick;

        public VolumeInterpolation(float startingVolume, float endingVolume, int ticks) {
            this.ticks = ticks;
            this.deltaPerTick = (endingVolume - startingVolume) / ticks;
        }

        public void tick(NoxesiumSoundInstance soundInstance) {
            soundInstance.volume = soundInstance.volume + deltaPerTick;
            ticks--;
            if (ticks <= 0) {
                soundInstance.volumeInterpolation = null;
            }
        }
    }

    /**
     * Applies the start offset to a given audio channel
     */
    public void applyStartOffset(ChannelAccess.ChannelHandle channelHandle) {
        channelHandle.execute(channel -> {
            AL10.alSourcef(((ChannelExt)channel).getSource(), AL11.AL_SEC_OFFSET, this.startOffset);
        });
    }
}
