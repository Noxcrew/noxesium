package com.noxcrew.noxesium.feature.sounds;

import com.noxcrew.noxesium.NoxesiumModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages and stores the currently playing sounds
 */
public class NoxesiumSoundModule implements NoxesiumModule {
    private final Map<Integer, NoxesiumSoundInstance> sounds = new HashMap<>();
    private static NoxesiumSoundModule instance;
    public static NoxesiumSoundModule getInstance() {
        if (instance == null) {
            instance = new NoxesiumSoundModule();
        }
        return instance;
    }

    /**
     * Plays a given sound instance and stores it, so it
     * can be modified later
     * @param instance The sound instance to play
     */
    public void play(int id, NoxesiumSoundInstance instance) {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();

        if (sounds.containsKey(id)) {
            NoxesiumSoundInstance sound = sounds.get(id);
            soundManager.stop(sound);
        }
        sounds.put(id, instance);
        soundManager.play(instance);
    }

    @Override
    public void onQuitServer() {
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

}
