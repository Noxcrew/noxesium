package com.noxcrew.noxesium.api.player;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.network.handshake.HandshakeState;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;

/**
 * Manages all players that have completed the Noxesium handshake.
 */
public class NoxesiumPlayerManager {
    protected static NoxesiumPlayerManager instance;

    /**
     * Returns the singleton instance of this class.
     */
    public static NoxesiumPlayerManager getInstance() {
        Preconditions.checkNotNull(instance, "Cannot get player manager instance before it is defined");
        return instance;
    }

    /**
     * Sets the player manager instance.
     */
    public static void setInstance(NoxesiumPlayerManager instance) {
        Preconditions.checkState(
                NoxesiumPlayerManager.instance == null, "Cannot set the player manager instance twice!");
        NoxesiumPlayerManager.instance = instance;
    }

    private final Map<UUID, NoxesiumServerPlayer> players = new ConcurrentHashMap<>();

    /**
     * Returns all online players.
     */
    public Collection<NoxesiumServerPlayer> getAllPlayers() {
        return players.values();
    }

    /**
     * Registers a new player with the given UUID and starting data.
     */
    public void registerPlayer(UUID uniqueId, NoxesiumServerPlayer player) {
        Preconditions.checkState(!players.containsKey(uniqueId), "Cannot register player '" + uniqueId + "' twice!");
        players.put(uniqueId, player);
    }

    /**
     * Removes data stored for the given player.
     */
    public void unregisterPlayer(UUID uniqueId) {
        var player = players.remove(uniqueId);
        if (player != null) {
            player.setHandshakeState(HandshakeState.NONE);
        }
    }

    /**
     * Returns the Noxesium data for the given player if it exists.
     */
    @Nullable
    public NoxesiumServerPlayer getPlayer(UUID uniqueId) {
        return players.get(uniqueId);
    }
}
