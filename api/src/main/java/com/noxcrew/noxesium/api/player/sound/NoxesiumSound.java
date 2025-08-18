package com.noxcrew.noxesium.api.player.sound;

import com.noxcrew.noxesium.api.network.NoxesiumClientboundNetworking;
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundModifyPacket;
import com.noxcrew.noxesium.core.network.clientbound.ClientboundCustomSoundStopPacket;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Stores information on a Noxesium sound send to a player.
 */
public class NoxesiumSound {
    @NotNull
    private final NoxesiumServerPlayer player;

    private final int id;
    private boolean stopped = false;

    public NoxesiumSound(@NotNull final NoxesiumServerPlayer player, @NotNull final int id) {
        this.player = player;
        this.id = id;
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
     * Changes the volume of this sound to the given value over the given amount of time.
     * Will start at the current volume of the sound.
     *
     * @param volume The volume to interpolate to.
     * @param ticks  The amount of ticks to interpolate for.
     */
    public void changeVolume(float volume, int ticks) {
        if (stopped) return;
        NoxesiumClientboundNetworking.send(
                player, new ClientboundCustomSoundModifyPacket(id, volume, ticks, Optional.empty()));
    }

    /**
     * Changes the volume of this sound to the given value over the given amount of time.
     *
     * @param from  The volume to interpolate from.
     * @param to    The volume to interpolate to.
     * @param ticks The amount of ticks to interpolate for.
     */
    public void changeVolume(float from, float to, int ticks) {
        if (stopped) return;
        NoxesiumClientboundNetworking.send(
                player, new ClientboundCustomSoundModifyPacket(id, to, ticks, Optional.of(from)));
    }

    /**
     * Stops playing this sound.
     */
    public void stopSound() {
        if (stopped) return;
        stopped = true;
        NoxesiumClientboundNetworking.send(player, new ClientboundCustomSoundStopPacket(id));
    }
}
