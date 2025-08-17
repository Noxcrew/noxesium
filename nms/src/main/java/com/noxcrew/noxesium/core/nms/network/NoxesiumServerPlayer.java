package com.noxcrew.noxesium.core.nms.network;

import com.noxcrew.noxesium.api.network.EntrypointProtocol;
import com.noxcrew.noxesium.core.client.setting.ClientSettings;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores information on a player connected to a server running Noxesium's
 * server-side API.
 */
public class NoxesiumServerPlayer {
    @NotNull
    private final UUID uniqueId;

    @NotNull
    private final List<EntrypointProtocol> entrypoints;

    @NotNull
    private final List<String> supportedEntrypointIds;

    @Nullable
    private ClientSettings settings;

    public NoxesiumServerPlayer(@NotNull final UUID uniqueId, @NotNull final List<EntrypointProtocol> entrypoints) {
        this.uniqueId = uniqueId;
        this.entrypoints = entrypoints;
        this.supportedEntrypointIds =
                entrypoints.stream().map(EntrypointProtocol::id).toList();
    }

    /**
     * Returns the UUID of this player.
     */
    @NotNull
    public UUID getUniqueId() {
        return uniqueId;
    }

    /**
     * Returns a list of all entrypoints this client is using and can communicate
     * with the server through. This includes the specific protocol version being
     * used by the client.
     */
    @NotNull
    public List<EntrypointProtocol> getSupportedEntrypoints() {
        return entrypoints;
    }

    /**
     * Returns a list of the ids of all entrypoints this client can receive.
     */
    @NotNull
    public List<String> getSupportedEntrypointIds() {
        return supportedEntrypointIds;
    }

    /**
     * Returns the last received settings defined by this client.
     */
    @Nullable
    public ClientSettings getClientSettings() {
        return settings;
    }

    /**
     * Updates the client settings of this client.
     */
    public void updateClientSettings(@NotNull ClientSettings settings) {
        this.settings = settings;
    }
}
