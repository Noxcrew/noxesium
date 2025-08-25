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
        float volume,
        float pitch,
        float offset,
        boolean looping,
        boolean attenuation,
        boolean ignoreIfPlaying,
        Optional<Vector3f> position,
        Optional<Integer> entityId)
        implements NoxesiumPacket {}
