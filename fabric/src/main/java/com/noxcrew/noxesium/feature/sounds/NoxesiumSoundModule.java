package com.noxcrew.noxesium.feature.sounds;

import com.noxcrew.noxesium.NoxesiumModule;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages and stores the currently playing sounds
 */
public class NoxesiumSoundModule implements NoxesiumModule {

    private final Map<Integer, NoxesiumSoundInstance> sounds = new HashMap<>();

    /**
     * Plays a given sound instance and stores it by its id so it
     * can be modified later.
     *
     * @param id              The id to play the sound under
     * @param instance        The sound instance to play
     * @param ignoreIfPlaying Whether to ignore this request if the sound is already playing
     */
    public void play(int id, NoxesiumSoundInstance instance, boolean ignoreIfPlaying) {
        var soundManager = Minecraft.getInstance().getSoundManager();

        // Deal with whatever sound is currently playing
        var currentSound = getSound(id);
        if (currentSound != null && ignoreIfPlaying) return;
        if (currentSound != null) {
            soundManager.stop(currentSound);
            sounds.remove(id);
        }

        // Play the new sound
        sounds.put(id, instance);
        soundManager.play(instance);
    }

    @Override
    public void onQuitServer() {
        // Clear all information about pending sounds on quit
        sounds.clear();
    }


    /**
     * Returns a currently playing custom sound
     */
    @Nullable
    public NoxesiumSoundInstance getSound(int id) {
        NoxesiumSoundInstance soundInstance = sounds.get(id);
        if (soundInstance.isStopped()) {
            sounds.remove(id);
            return null;
        }
        return soundInstance;
    }

    /**
     * Stops the sound with the given id, if one is playing.
     *
     * @param id The id of the sound to stop
     */
    public void stopSound(int id) {
        var sound = getSound(id);
        if (sound != null) {
            var soundManager = Minecraft.getInstance().getSoundManager();
            soundManager.stop(sound);
            sounds.remove(id);
        }
    }
}
