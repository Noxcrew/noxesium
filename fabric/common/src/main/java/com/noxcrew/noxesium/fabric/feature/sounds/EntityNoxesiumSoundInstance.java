package com.noxcrew.noxesium.fabric.feature.sounds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

/**
 * The sound instance for custom modifiable sounds
 */
public class EntityNoxesiumSoundInstance extends NoxesiumSoundInstance {

    private final Entity entity;

    public EntityNoxesiumSoundInstance(
            ResourceLocation sound,
            SoundSource soundSource,
            Entity entity,
            float volume,
            float pitch,
            boolean looping,
            boolean attenuation,
            float startOffset) {
        super(sound, soundSource, entity.position(), volume, pitch, looping, attenuation, startOffset);
        this.entity = entity;
    }

    /**
     * Overrides whether this sound can be played to be based on whether the
     * entity is not silent.
     */
    @Override
    public boolean canPlaySound() {
        return !this.entity.isSilent();
    }

    /**
     * Updates the current position of the sound based on the movement of the entity.
     */
    @Override
    public void tick() {
        if (this.entity.isRemoved()) {
            this.stop();
        } else {
            this.x = (float) this.entity.getX();
            this.y = (float) this.entity.getY();
            this.z = (float) this.entity.getZ();
        }
    }
}
