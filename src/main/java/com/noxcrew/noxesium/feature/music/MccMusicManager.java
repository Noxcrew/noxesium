package com.noxcrew.noxesium.feature.music;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

/**
 * Manages playing background music during games on MCC Event and Island.
 */
public class MccMusicManager {

    @Nullable
    private SoundInstance currentMusic;

    /**
     * Starts playing the given music track on the given [slider].
     */
    public void startPlaying(SoundEvent event, String slider) {
        currentMusic = createInstance(event, slider);
        if (currentMusic.getSound() != SoundManager.EMPTY_SOUND) {
            Minecraft.getInstance().getSoundManager().play(currentMusic);
        }
    }

    /**
     * Stops playing the current music track.
     */
    public void stopPlaying() {
        if (currentMusic != null) {
            Minecraft.getInstance().getSoundManager().stop(currentMusic);
            currentMusic = null;
        }
    }

    /**
     * Creates a new sound instance to play.
     */
    public static SimpleSoundInstance createInstance(SoundEvent sound, String category) {
        var source = Stream.of(SoundSource.values()).filter(f -> f.getName().equals(category)).findFirst().orElseThrow();
        return new SimpleSoundInstance(sound.getLocation(), source, 1.0F, 1.0F, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true);
    }
}
