package com.noxcrew.noxesium.feature.sounds;

import com.noxcrew.noxesium.NoxesiumModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages and stores the currently playing sounds
 */
public class NoxesiumSoundManager implements NoxesiumModule {
    private final Map<ResourceLocation, NoxesiumSoundInstance> sounds = new HashMap<>();
    private static NoxesiumSoundManager instance;
    public static NoxesiumSoundManager getInstance() {
        if (instance == null) {
            instance = new NoxesiumSoundManager();
        }
        return instance;
    }

    /**
     * Plays a given sound instance and stores it, so it
     * can be modified later
     * @param instance The sound instance to play
     */
    public void play(NoxesiumSoundInstance instance) {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();

        if (sounds.containsKey(instance.getLocation())) {
            NoxesiumSoundInstance sound = sounds.get(instance.getLocation());
            soundManager.stop(sound);
        }
        sounds.put(instance.getLocation(), instance);
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
    public NoxesiumSoundInstance getSound(ResourceLocation location) {
        NoxesiumSoundInstance soundInstance = sounds.get(location);
        if (soundInstance.isStopped()) {
            sounds.remove(location);
            return null;
        }
        return soundInstance;
    }

}
