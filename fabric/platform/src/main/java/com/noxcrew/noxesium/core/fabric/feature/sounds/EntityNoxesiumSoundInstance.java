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
    private final boolean local;

    public EntityNoxesiumSoundInstance(
            Key sound,
            Sound.Source soundSource,
            Entity entity,
            float volume,
            float pitch,
            float offset,
            boolean looping,
            boolean attenuation,
            boolean local
            ) {
        super(
                sound,
                soundSource,
                new Vector3f((float) entity.getX(), (float) entity.getY(), (float) entity.getZ()),
                volume,
                pitch,
                offset,
                looping,
                attenuation);
        this.entity = entity;
        this.local = local;
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
        if (!local && this.entity.isRemoved()) {
            this.stop();
        } else {
            this.x = (float) this.entity.getX();
            this.y = (float) this.entity.getY();
            this.z = (float) this.entity.getZ();
        }
    }
}
