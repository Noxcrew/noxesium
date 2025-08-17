package com.noxcrew.noxesium.api.player;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.network.EntrypointProtocol;
import com.noxcrew.noxesium.api.network.NoxesiumClientboundNetworking;
import com.noxcrew.noxesium.api.network.handshake.HandshakeState;
import com.noxcrew.noxesium.core.client.setting.ClientSettings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
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
    private final String username;

    @NotNull
    private final Component displayName;

    @NotNull
    private HandshakeState handshakeState = HandshakeState.NONE;

    @NotNull
    private final List<EntrypointProtocol> supportedEntrypoints = new ArrayList<>();

    @NotNull
    private final List<String> supportedEntrypointIds = new ArrayList<>();

    @NotNull
    private final List<Integer> pendingRegistrySyncs = new ArrayList<>();

    @Nullable
    private ClientSettings settings;

    public NoxesiumServerPlayer(
            @NotNull final UUID uniqueId, @NotNull final String username, @NotNull final Component displayName) {
        this.uniqueId = uniqueId;
        this.username = username;
        this.displayName = displayName;
    }

    /**
     * Returns the UUID of this player.
     */
    @NotNull
    public UUID getUniqueId() {
        return uniqueId;
    }

    /**
     * Returns the username of this player.
     */
    @NotNull
    public String getUsername() {
        return username;
    }

    /**
     * Returns the display name of this player.
     */
    @NotNull
    public Component getDisplayName() {
        return displayName;
    }

    /**
     * Returns the current handshake state.
     */
    @NotNull
    public HandshakeState getHandshakeState() {
        return handshakeState;
    }

    /**
     * Sets the current handshake state.
     */
    public void setHandshakeState(HandshakeState state) {
        this.handshakeState = state;
    }

    /**
     * Adds the given entrypoints to this player.
     */
    public void addEntrypoints(Collection<EntrypointProtocol> entrypoints) {
        for (var entrypoint : entrypoints) {
            supportedEntrypoints.add(entrypoint);
            supportedEntrypointIds.add(entrypoint.id());
        }
    }

    /**
     * Returns the base version of the mod.
     */
    public String getBaseVersion() {
        return supportedEntrypoints.stream()
                .filter(it -> it.id().equals(NoxesiumReferences.COMMON_ENTRYPOINT))
                .map(EntrypointProtocol::rawVersion)
                .findAny()
                .orElse("unknown");
    }

    /**
     * Returns a list of all entrypoints this client is using and can communicate
     * with the server through. This includes the specific protocol version being
     * used by the client.
     */
    @NotNull
    public List<EntrypointProtocol> getSupportedEntrypoints() {
        return supportedEntrypoints;
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

    /**
     * Adds a new identifier to wait for with registry syncs.
     */
    public void awaitRegistrySync(int id) {
        pendingRegistrySyncs.add(id);
    }

    /**
     * Handles acknowledgement of a registry synchronization.
     */
    public boolean acknowledgeRegistrySync(int id) {
        if (pendingRegistrySyncs.contains(id)) {
            pendingRegistrySyncs.remove((Object) id);
            return true;
        }
        return false;
    }

    /**
     * Returns if the handshake has been completed and informs
     * the client if it is.
     */
    public boolean isHandshakeCompleted() {
        // If we are waiting some registry sync to complete we cannot complete the handshake
        if (!pendingRegistrySyncs.isEmpty()) return false;

        // Check if every entrypoint has at least one channel registered
        var registeredChannels = NoxesiumClientboundNetworking.getInstance().getRegisteredChannels(this);
        for (var protocol : supportedEntrypoints) {
            var entrypoint = NoxesiumApi.getInstance().getEntrypoint(protocol.id());

            // This should never occur but just in case we just prevent the handshake from completing!
            if (entrypoint == null) return false;

            // Check for all channels in this entrypoint's collection if none of them are registered
            // we still need to wait!
            if (entrypoint instanceof NoxesiumEntrypoint nmsEntrypoint) {
                var channels = nmsEntrypoint.getPacketCollections().stream()
                        .flatMap(it -> it.getPackets().stream())
                        .map(it -> it.id().toString());
                if (channels.noneMatch(registeredChannels::contains)) return false;
            }
        }
        return true;
    }
}
