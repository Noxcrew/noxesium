package com.noxcrew.noxesium.core.network.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import java.util.Optional;

/**
 * Sent by a server to change the volume of a sound. The interpolation time can be
 * used to fade the sound up or down over an amount of ticks.
 *
 * @param startVolume An optional volume to start the interpolation from. If absent the current volume of the sound is used instead.
 */
public record ClientboundCustomSoundModifyPacket(
        int id, float volume, int interpolationTicks, Optional<Float> startVolume) implements NoxesiumPacket {}
