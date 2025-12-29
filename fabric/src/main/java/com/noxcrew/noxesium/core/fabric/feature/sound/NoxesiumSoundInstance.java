package com.noxcrew.noxesium.core.fabric.feature.sound;

import com.noxcrew.noxesium.core.fabric.mixin.feature.sound.ChannelExt;
import java.util.Optional;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.modcommon.impl.GameEnums;
import net.kyori.adventure.sound.Sound;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.joml.Vector3fc;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

/**
 * The sound instance for custom modifiable sounds
 */
public class NoxesiumSoundInstance extends AbstractTickableSoundInstance {

    private final float startOffset;
    private VolumeInterpolation volumeInterpolation;

    public NoxesiumSoundInstance(
            Key sound,
            Sound.Source soundSource,
            Vector3fc initialPosition,
            float volume,
            float pitch,
            float offset,
            boolean looping,
            boolean attenuation) {
        super(
                SoundEvent.createVariableRangeEvent(Identifier.parse(sound.asString())),
                GameEnums.SOUND_SOURCE.toMinecraft(soundSource),
                SoundInstance.createUnseededRandom());
        this.x = initialPosition.x();
        this.y = initialPosition.y();
        this.z = initialPosition.z();
        this.volume = volume;
        this.pitch = pitch;
        this.looping = looping;
        this.startOffset = offset;

        if (attenuation) {
            this.attenuation = Attenuation.LINEAR;
        } else {
            this.attenuation = Attenuation.NONE;
        }
    }

    /**
     * Returns the offset at which this sound should start.
     */
    public float getStartOffset() {
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
     * Applies the start offset to a given audio channel, ignoring the given bufferd time.
     */
    public void applyStartOffset(ChannelAccess.ChannelHandle channelHandle, int bufferedSeconds) {
        channelHandle.execute(channel -> {
            AL10.alSourcef(((ChannelExt) channel).getSource(), AL11.AL_SEC_OFFSET, this.startOffset - bufferedSeconds);
        });
    }
}
