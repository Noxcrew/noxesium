package com.noxcrew.noxesium.api.player.sound;

import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundModifyPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundStartPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundStopPacket;
import java.util.Optional;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Stores information on a Noxesium sound send to a player.
 */
public class NoxesiumSound {
    private final int id;

    @NotNull
    private final NoxesiumServerPlayer player;

    @NotNull
    private Key sound;

    @NotNull
    private Sound.Source source;

    private float volume;
    private float pitch;
    private float offset;
    private boolean looping;
    private boolean attenuation;

    @Nullable
    private Vector3f position;

    @Nullable
    private Integer entityId;

    private boolean stopped = false;

    public NoxesiumSound(
            @NotNull final NoxesiumServerPlayer player,
            final int id,
            @NotNull Key sound,
            @NotNull Sound.Source source,
            float volume,
            float pitch,
            float offset,
            boolean looping,
            boolean attenuation,
            @Nullable Vector3f position,
            @Nullable Integer entityId) {
        this.player = player;
        this.id = id;
        this.sound = sound;
        this.source = source;
        this.volume = volume;
        this.pitch = pitch;
        this.offset = offset;
        this.looping = looping;
        this.attenuation = attenuation;
        this.position = position;
        this.entityId = entityId;
    }

    /**
     * Returns the player this sound is being sent to.
     */
    @NotNull
    public NoxesiumServerPlayer getPlayer() {
        return player;
    }

    /**
     * Returns the id of this sound.
     */
    public int getId() {
        return id;
    }

    /**
     * Replays this sound to the player starting at the last set volume.
     * If `ignoreIfPlaying` is true, the sound is not restarted if it
     * was already being played by the client.
     */
    public void play(boolean ignoreIfPlaying) {
        player.sendPacket(new ClientboundCustomSoundStartPacket(
                id,
                sound,
                source,
                volume,
                pitch,
                offset,
                looping,
                attenuation,
                ignoreIfPlaying,
                Optional.ofNullable(position),
                Optional.ofNullable(entityId)));
    }

    /**
     * Changes the volume of this sound to the given value over the given amount of time.
     * Will start at the current volume of the sound.
     *
     * @param volume The volume to interpolate to.
     * @param ticks  The amount of ticks to interpolate for.
     */
    public void interpolateVolume(float volume, int ticks) {
        this.volume = volume;
        if (stopped) return;
        player.sendPacket(new ClientboundCustomSoundModifyPacket(id, volume, ticks, Optional.empty()));
    }

    /**
     * Changes the volume of this sound to the given value over the given amount of time.
     *
     * @param from  The volume to interpolate from.
     * @param to    The volume to interpolate to.
     * @param ticks The amount of ticks to interpolate for.
     */
    public void interpolateVolume(float from, float to, int ticks) {
        this.volume = to;
        if (stopped) return;
        player.sendPacket(new ClientboundCustomSoundModifyPacket(id, to, ticks, Optional.of(from)));
    }

    /**
     * Stops playing this sound.
     */
    public void stopSound() {
        if (stopped) return;
        stopped = true;
        player.sendPacket(new ClientboundCustomSoundStopPacket(id));
    }

    /**
     * Returns the current sound event id.
     */
    public @NotNull Key getSound() {
        return sound;
    }

    /**
     * Sets the sound event id to the given value.
     * Changes are not applied until the next time the sound is played.
     */
    public void setSound(@NotNull Key sound) {
        this.sound = sound;
    }

    /**
     * Returns the current sound source.
     */
    public @NotNull Sound.Source getSource() {
        return source;
    }

    /**
     * Sets the sound source to the given value.
     * Changes are not applied until the next time the sound is played.
     */
    public void setSource(@NotNull Sound.Source source) {
        this.source = source;
    }

    /**
     * Returns the current pitch.
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Sets the sound pitch to the given value.
     * Changes are not applied until the next time the sound is played.
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /**
     * Returns the current offset into the sound effect
     * that the client will start playing from.
     */
    public float getOffset() {
        return offset;
    }

    /**
     * Sets the sound offset to the given value.
     * Changes are not applied until the next time the sound is played.
     */
    public void setOffset(float offset) {
        this.offset = offset;
    }

    /**
     * Returns whether the sound will loop.
     */
    public boolean isLooping() {
        return looping;
    }

    /**
     * Sets whether to loop to the given value.
     * Changes are not applied until the next time the sound is played.
     */
    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    /**
     * Returns the current volume.
     */
    public float getVolume() {
        return volume;
    }

    /**
     * Sets the volume to the given value. If the sound is being played
     * the volume is immediately updated.
     */
    public void setVolume(float volume) {
        this.volume = volume;
        if (stopped) return;
        player.sendPacket(new ClientboundCustomSoundModifyPacket(id, volume, 0, Optional.empty()));
    }

    /**
     * Returns the sound has attentuation, that is the sound's volume decreases
     * based on the distance from the source position.
     */
    public boolean isAttenuation() {
        return attenuation;
    }

    /**
     * Sets whether to attenuate the sound to the given value.
     * Changes are not applied until the next time the sound is played.
     */
    public void setAttenuation(boolean attenuation) {
        this.attenuation = attenuation;
    }

    /**
     * Returns the current position where the sound originates from. If no position
     * or entity is given the sound is played attached to the local player.
     */
    public @Nullable Vector3f getPosition() {
        return position;
    }

    /**
     * Sets the position of the sound to the given value.
     * Changes are not applied until the next time the sound is played.
     */
    public void setPosition(@Nullable Vector3f position) {
        this.position = position;
    }

    /**
     * Returns the current entity that the sound originates from. If no position
     * or entity is given the sound is played attached to the local player.
     * <p>
     * If this entity dies the sound event is stopped. It is recommended to play
     * sounds intended for the target player with no entity id as such sounds are
     * not stopped when the local player dies.
     */
    public @Nullable Integer getEntityId() {
        return entityId;
    }

    /**
     * Sets the target entity of the sound to the given value.
     * Changes are not applied until the next time the sound is played.
     */
    public void setEntityId(@Nullable Integer entityId) {
        this.entityId = entityId;
    }
}
