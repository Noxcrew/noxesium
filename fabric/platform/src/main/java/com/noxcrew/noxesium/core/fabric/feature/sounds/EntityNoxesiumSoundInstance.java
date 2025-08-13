package com.noxcrew.noxesium.core.fabric.feature.sounds;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

/**
 * The sound instance for custom modifiable sounds
 */
public class EntityNoxesiumSoundInstance extends NoxesiumSoundInstance {

    private final Entity entity;

    public EntityNoxesiumSoundInstance(
            Key sound,
            Sound.Source soundSource,
            Entity entity,
            float volume,
            float pitch,
            boolean looping,
            boolean attenuation,
            int startOffset) {
        super(
                sound,
                soundSource,
                new Vector3f((float) entity.getX(), (float) entity.getY(), (float) entity.getZ()),
                volume,
                pitch,
                looping,
                attenuation,
                startOffset);
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
