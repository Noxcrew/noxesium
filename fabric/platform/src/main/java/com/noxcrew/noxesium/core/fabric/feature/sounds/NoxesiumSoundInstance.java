package com.noxcrew.noxesium.core.fabric.feature.sounds;

import com.noxcrew.noxesium.core.fabric.mixin.sound.ChannelExt;
import java.util.Optional;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.modcommon.impl.GameEnums;
import net.kyori.adventure.sound.Sound;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

/**
 * The sound instance for custom modifiable sounds
 */
public class NoxesiumSoundInstance extends AbstractTickableSoundInstance {

    private final int startOffset;
    private VolumeInterpolation volumeInterpolation;

    public NoxesiumSoundInstance(
            Key sound,
            Sound.Source soundSource,
            Vector3f initialPosition,
            float volume,
            float pitch,
            boolean looping,
            boolean attenuation,
            int startOffset) {
        super(
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse(sound.asString())),
                GameEnums.SOUND_SOURCE.toMinecraft(soundSource),
                SoundInstance.createUnseededRandom());
        this.x = initialPosition.x();
        this.y = initialPosition.y();
        this.z = initialPosition.z();
        this.volume = volume;
        this.pitch = pitch;
        this.looping = looping;
        this.startOffset = startOffset;

        if (attenuation) {
            this.attenuation = Attenuation.LINEAR;
        } else {
            this.attenuation = Attenuation.NONE;
        }
    }

    /**
     * Returns the offset at which this sound should start.
     */
    public int getStartOffset() {
        return startOffset;
    }

    @Override
    public void tick() {
        if (volumeInterpolation != null) {
            var newVolume = volumeInterpolation.tick();
            if (newVolume == null) {
                volumeInterpolation = null;
            } else {
                this.volume = newVolume;
            }
        }
    }

    /**
     * Sets the volume over a specified time
     *
     * @param volume      The new volume to set to
     * @param startVolume The volume to start interpolation from.
     * @param ticks       The fade time
     */
    public void setVolume(float volume, Optional<Float> startVolume, int ticks) {
        if (ticks <= 0) {
            this.volume = volume;
        } else {
            this.volumeInterpolation = new VolumeInterpolation(startVolume.orElse(this.volume), volume, ticks);
        }
    }

    /**
     * Applies the start offset to a given audio channel
     */
    public void applyStartOffset(ChannelAccess.ChannelHandle channelHandle) {
        channelHandle.execute(channel -> {
            AL10.alSourcef(((ChannelExt) channel).getSource(), AL11.AL_SEC_OFFSET, this.startOffset);
        });
    }
}
