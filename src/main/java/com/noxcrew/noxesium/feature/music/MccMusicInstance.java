package com.noxcrew.noxesium.feature.music;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * A sound instance that can start completely silent. Allows tracks to start even when the music volume is set to zero.
 * Muted tracks are still paused due to SoundEngine changes but it allows them to start and play while muted.
 */
public class MccMusicInstance extends SimpleSoundInstance {

    public MccMusicInstance(SoundEvent soundEvent, SoundSource soundSource, float f, float g, RandomSource randomSource, double d, double e, double h) {
        super(soundEvent, soundSource, f, g, randomSource, d, e, h);
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
