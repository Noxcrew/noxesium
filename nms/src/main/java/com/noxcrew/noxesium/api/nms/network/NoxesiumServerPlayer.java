package com.noxcrew.noxesium.api.nms.network;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.network.EntrypointProtocol;
import com.noxcrew.noxesium.api.nms.NmsNoxesiumEntrypoint;
import com.noxcrew.noxesium.core.client.setting.ClientSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores information on a player connected to a server running Noxesium's
 * server-side API.
 */
public class NoxesiumServerPlayer {
    @NotNull
    private final ServerPlayer player;

    @NotNull
    private final List<EntrypointProtocol> entrypoints;

    @NotNull
    private final List<String> supportedEntrypointIds;

    @NotNull
    private final List<Integer> pendingRegistrySyncs = new ArrayList<>();

    @Nullable
    private ClientSettings settings;

    public NoxesiumServerPlayer(
            @NotNull final ServerPlayer player, @NotNull final List<EntrypointProtocol> entrypoints) {
        this.player = player;
        this.entrypoints = entrypoints;
        this.supportedEntrypointIds =
                entrypoints.stream().map(EntrypointProtocol::id).toList();
    }

    /**
     * Returns the UUID of this player.
     */
    @NotNull
    public UUID getUniqueId() {
        return player.getUUID();
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
            pendingRegistrySyncs.remove(id);
            return true;
        }
        return false;
    }

    /**
     * Returns the NMS player instance for this player.
     */
    @NotNull
    public ServerPlayer getNmsPlayer() {
        return player;
    }

    /**
     * Returns if the handshake has been completed and informs
     * the client if it is.
     */
    public boolean isHandshakeCompleted() {
        // If we are waiting some registry sync to complete we cannot complete the handshake
        if (!pendingRegistrySyncs.isEmpty()) return false;

        // Check if every entrypoint has at least one channel registered
        var registeredChannels = NoxesiumClientboundNetworking.getInstance().getRegisteredChannels(player);
        for (var protocol : entrypoints) {
            var entrypoint = NoxesiumApi.getInstance().getEntrypoint(protocol.id());

            // This should never occur but just in case we just prevent the handshake from completing!
            if (entrypoint == null) return false;

            // Check for all channels in this entrypoint's collection if none of them are registered
            // we still need to wait!
            if (entrypoint instanceof NmsNoxesiumEntrypoint nmsEntrypoint) {
                var channels = nmsEntrypoint.getPacketCollections().stream()
                        .flatMap(it -> it.getPackets().stream())
                        .map(it -> it.id().toString());
                if (channels.noneMatch(registeredChannels::contains)) return false;
            }
        }
        return true;
    }
}
