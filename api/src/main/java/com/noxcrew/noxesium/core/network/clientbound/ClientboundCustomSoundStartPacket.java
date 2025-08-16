package com.noxcrew.noxesium.core.network.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import java.util.Optional;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.joml.Vector3f;

/**
 * Sent by a server to start a Noxesium custom sound. If a sound with the same id
 * is already playing, that sound will be stopped.
 *
 * @param position        The position where the sound is playing, can be null.
 * @param entityId        The entity that the sound is playing relative to, can be null.
 * @param attenuation     Whether this sound has attenuation. If `false`, the sound is played at the same
 *                        volume regardless of distance to the position. Should be `true` for most sounds.
 * @param ignoreIfPlaying Whether to ignore playing the sound if the id is already playing another sound.
 * @param offset          The offset of the sound in seconds.
 */
public record ClientboundCustomSoundStartPacket(
        int id,
        Key sound,
        Sound.Source source,
        boolean looping,
        boolean attenuation,
        boolean ignoreIfPlaying,
        float volume,
        float pitch,
        Optional<Vector3f> position,
        Optional<Integer> entityId,
        Optional<Long> unix,
        Optional<Integer> offset)
        implements NoxesiumPacket {
    /**
     * Determines the offset to start the sound at. This can be defined through either a unix
     * timestamp or an offset value.
     */
    public int determineOffset() {
        return unix.map(aLong -> (int) (Math.max(0, System.currentTimeMillis() - aLong) / 1000))
                .orElseGet(() -> offset.orElse(0));
    }
}
