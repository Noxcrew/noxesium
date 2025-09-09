package com.noxcrew.noxesium.api.network.handshake;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.NoxesiumClientboundNetworking;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeCancelPacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeCompletePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundRegistryContentUpdatePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundRegistryIdsUpdatePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundRegistryUpdateResultPacket;
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager;
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.ServerNoxesiumRegistry;
import com.noxcrew.noxesium.api.registry.SynchronizedServerNoxesiumRegistry;
import com.noxcrew.noxesium.api.util.EncryptionUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Performs handshaking with the client-side to be run from the server-side.
 */
public abstract class NoxesiumServerHandshaker {
    /**
     * Pending packets to be sent to clients after they register their plugin channels.
     */
    protected final Map<UUID, NoxesiumPacket> pendingPackets = new HashMap<>();

    /**
     * All players who we are about to check handshake completion for. Tracked to prevent task
     * spamming.
     */
    private final Set<UUID> pendingChecks = new HashSet<>();

    /**
     * Stores universally unique identifiers for registry updates.
     */
    private final MutableInt registryUpdateIdentifier = new MutableInt();

    /**
     * Registers the handshaker.
     */
    public void register() {
        // Register the handshaking packets so clients know they can authenticate here!
        HandshakePackets.INSTANCE.register(null);

        // Respond to incoming handshake packets (except the initial one!)
        HandshakePackets.SERVERBOUND_HANDSHAKE_ACKNOWLEDGE.addListener(this, (reference, packet, playerId) -> {
            reference.handleHandshakeAcknowledge(playerId, packet);
        });
        HandshakePackets.SERVERBOUND_HANDSHAKE_CANCEL.addListener(this, (reference, packet, playerId) -> {
            reference.onPlayerDisconnect(playerId);
        });
        HandshakePackets.SERVERBOUND_REGISTRY_UPDATE_RESULT.addListener(this, (reference, packet, playerId) -> {
            reference.onRegistryUpdateResult(playerId, packet);
        });
    }

    /**
     * Ticks this handshaker, prompting it to check if any registries need to be synchronized
     * with online clients.
     */
    public void tick() {
        // Determine if some registry is dirty.
        var dirtyRegistries = new HashSet<SynchronizedServerNoxesiumRegistry<?>>();
        NoxesiumRegistries.REGISTRIES.forEach(registry -> {
            if (registry instanceof SynchronizedServerNoxesiumRegistry<?> synchronizedServerNoxesiumRegistry) {
                if (synchronizedServerNoxesiumRegistry.isDirty()) {
                    dirtyRegistries.add(synchronizedServerNoxesiumRegistry);
                }
            }
        });
        if (dirtyRegistries.isEmpty()) return;

        // Go through all players and see if we can send them updates.
        for (var player : NoxesiumPlayerManager.getInstance().getAllPlayers()) {
            // Ignore players that have not started receiving registries yet
            if (player.getHandshakeState() != HandshakeState.AWAITING_REGISTRIES
                    && player.getHandshakeState() != HandshakeState.COMPLETE) continue;

            // Send these players the additional registry data
            var entrypoints = player.getSupportedEntrypointIds();
            for (var registry : dirtyRegistries) {
                // Ignore empty registries!
                var syncContents = registry.determineAllSyncableContent(entrypoints);
                if (syncContents.isEmpty()) continue;

                var id = registryUpdateIdentifier.getAndIncrement();
                player.awaitRegistrySync(id);
                player.sendPacket(new ClientboundRegistryContentUpdatePacket(id, syncContents));
            }
        }

        // Mark all registries as non-dirty
        dirtyRegistries.forEach(SynchronizedServerNoxesiumRegistry::clearPendingUpdates);
    }

    /**
     * Handles a client initiating a handshake with the server.
     */
    protected void handleHandshake(@NotNull NoxesiumServerPlayer player, @NotNull ServerboundHandshakePacket packet) {
        // Accept all incoming entry points and respond to them
        var entrypoints = new HashMap<String, String>();

        // Prepare the encrypted ids of each entrypoint
        var encryptedIds = new HashMap<String, NoxesiumEntrypoint>();
        for (var entrypoint : NoxesiumApi.getInstance().getActiveEntrypoints()) {
            encryptedIds.put(
                    EncryptionUtil.encrypt(entrypoint.getEncryptionKey(), List.of(entrypoint.getId()))
                            .getFirst(),
                    entrypoint);
        }

        // Determine which entrypoints the server is aware of
        for (var entry : packet.entrypoints().entrySet()) {
            // Ignore any entrypoints we do not recognise, decrypt the challenge for all others
            var entrypoint = encryptedIds.get(entry.getKey());
            if (entrypoint == null) continue;
            entrypoints.put(
                    entrypoint.getId(), EncryptionUtil.decrypt(entrypoint.getEncryptionKey(), entry.getValue()));
        }

        // If no entrypoints are supported by the server then we silently ignore this player. They may just not
        // be using any Noxesium entrypoints we know about! Do end the handshake properly!
        if (entrypoints.isEmpty()) {
            destroy(player.getUniqueId());
            return;
        }

        // If the handshake start is successful we register the player instance
        NoxesiumPlayerManager.getInstance().registerPlayer(player.getUniqueId(), player);

        // Determine the response based on the current state
        var acknowledgePacket = new ClientboundHandshakeAcknowledgePacket(entrypoints);
        if (NoxesiumClientboundNetworking.getInstance()
                .canReceive(player, ClientboundHandshakeAcknowledgePacket.class)) {
            // The client has already indicated it can receive the acknowledgment packet,
            // send it immediately!
            player.sendPacket(acknowledgePacket);
            player.setHandshakeState(HandshakeState.AWAITING_RESPONSE);
        } else {
            // The client hasn't sent that it can receive the acknowledgment packet yet, so
            // we put the packet in a pending list and wait for it.
            pendingPackets.put(player.getUniqueId(), acknowledgePacket);
            player.setHandshakeState(HandshakeState.NONE);
        }
    }

    /**
     * Handle a successful handshake acknowledgement, register all packets, and call events.
     */
    protected void handleHandshakeAcknowledge(
            @NotNull UUID uniqueId, @NotNull ServerboundHandshakeAcknowledgePacket packet) {
        var player = NoxesiumPlayerManager.getInstance().getPlayer(uniqueId);
        if (player == null) {
            NoxesiumApi.getLogger().error("Received handshake acknowledge for unknown player!");
            destroy(uniqueId);
            return;
        }
        if (!player.getHandshakeState().equals(HandshakeState.AWAITING_RESPONSE)) {
            NoxesiumApi.getLogger()
                    .error(
                            "Received registry contents while in '{}' state, destroying connection!",
                            player.getHandshakeState());
            destroy(uniqueId);
            return;
        }

        // Store the acknowledged entrypoints on this player's data
        player.addEntrypoints(packet.protocols());

        // Mark that we are now waiting to sync registries
        player.setHandshakeState(HandshakeState.AWAITING_REGISTRIES);

        // Start tasks for sending registries and receiving registration of plugin channels
        var entrypoints = player.getSupportedEntrypointIds();
        for (var registry : NoxesiumRegistries.REGISTRIES) {
            // We only have to synchronize server registries as only sided registries
            // use indices which have to be synchronized.
            if (registry instanceof SynchronizedServerNoxesiumRegistry<?> synchronizedRegistry) {
                // Ignore empty registries!
                var syncContents = synchronizedRegistry.determineAllSyncableContent(entrypoints);
                if (syncContents.isEmpty()) continue;

                var id = registryUpdateIdentifier.getAndIncrement();
                player.awaitRegistrySync(id);
                player.sendPacket(new ClientboundRegistryContentUpdatePacket(id, syncContents));
            } else if (registry instanceof ServerNoxesiumRegistry<?> serverRegistry) {
                // Ignore empty registries!
                var syncContents = serverRegistry.determineAllSyncableIds(entrypoints);
                if (syncContents.isEmpty()) continue;

                var id = registryUpdateIdentifier.getAndIncrement();
                player.awaitRegistrySync(id);
                player.sendPacket(new ClientboundRegistryIdsUpdatePacket(id, serverRegistry.id(), syncContents));
            }
        }

        // Test if all tasks are already complete
        if (player.isHandshakeCompleted()) {
            completeHandshake(player);
        }
    }

    /**
     * Handles a registry update acknowledgement.
     */
    protected void onRegistryUpdateResult(
            @NotNull UUID uniqueId, @NotNull ServerboundRegistryUpdateResultPacket packet) {
        var player = NoxesiumPlayerManager.getInstance().getPlayer(uniqueId);
        if (player == null) {
            NoxesiumApi.getLogger().error("Received registry update for unknown player!");
            destroy(uniqueId);
            return;
        }

        // Allow receiving registry updates when complete!
        if (!player.getHandshakeState().equals(HandshakeState.AWAITING_REGISTRIES)
                && !player.getHandshakeState().equals(HandshakeState.COMPLETE)) {
            NoxesiumApi.getLogger()
                    .error(
                            "Received registry update result while in '{}' state, destroying connection!",
                            player.getHandshakeState());
            destroy(uniqueId);
            return;
        }

        if (player.acknowledgeRegistrySync(packet.id())) {
            // Try to complete the handshaking, if applicable.
            if (player.isHandshakeCompleted()) {
                completeHandshake(player);
            }
        } else {
            NoxesiumApi.getLogger()
                    .error("Received invalid registry result update for id {}, destroying connection!", packet.id());
            destroy(uniqueId);
        }
    }

    /**
     * Destroys the connection with the given player.
     */
    protected void destroy(@NotNull UUID uniqueId) {
        NoxesiumClientboundNetworking.send(
                NoxesiumPlayerManager.getInstance().getPlayer(uniqueId), new ClientboundHandshakeCancelPacket());
        onPlayerDisconnect(uniqueId);
    }

    /**
     * Completes the handshake for the given player.
     */
    protected boolean completeHandshake(@NotNull NoxesiumServerPlayer player) {
        if (!player.getHandshakeState().equals(HandshakeState.AWAITING_REGISTRIES)) {
            NoxesiumApi.getLogger()
                    .error(
                            "Tried to complete handshake while in '{}' state, destroying connection!",
                            player.getHandshakeState());
            destroy(player.getUniqueId());
            return false;
        }

        // Move to the last handshake state and send the client a completion message
        player.setHandshakeState(HandshakeState.COMPLETE);
        player.sendPacket(new ClientboundHandshakeCompletePacket());
        NoxesiumApi.getLogger()
                .info(
                        "Authenticated {} on Noxesium {} with {} entrypoints",
                        player.getUsername(),
                        player.getBaseVersion(),
                        player.getSupportedEntrypoints().size());
        return true;
    }

    /**
     * Handles a player disconnecting or cancelling the handshake.
     */
    protected void onPlayerDisconnect(UUID uuid) {
        pendingPackets.remove(uuid);
        pendingChecks.remove(uuid);
        NoxesiumPlayerManager.getInstance().unregisterPlayer(uuid);
    }

    /**
     * Handles the given channel being registered for player, sending any pending
     * packets that were waiting for the channel to be registered.
     */
    protected void onChannelRegistered(@Nullable NoxesiumServerPlayer player, @NotNull String channel) {
        if (player == null) return;
        if (channel.equals(
                HandshakePackets.CLIENTBOUND_HANDSHAKE_ACKNOWLEDGE.id().toString())) {
            // Delay by a tick so the other handshake channels are also registered!
            runDelayed(() -> {
                if (!isConnected(player)) return;

                // The client has requested the handshake, but it has not yet been
                // sent the acknowledgement, send the pending packet now that we
                // definitely can!
                var packet = pendingPackets.remove(player.getUniqueId());
                if (packet == null) return;
                player.sendPacket(packet);
                player.setHandshakeState(HandshakeState.AWAITING_RESPONSE);
            });
        } else {
            // Check if the handshake should complete yet based on new channels being registered!
            if (player.getHandshakeState().equals(HandshakeState.AWAITING_REGISTRIES)) {
                // Prevent tasks from starting too often if we register a whole group of channels!
                if (pendingChecks.contains(player.getUniqueId())) return;
                pendingChecks.add(player.getUniqueId());

                runDelayed(() -> {
                    pendingChecks.remove(player.getUniqueId());

                    if (!isConnected(player)) return;
                    if (!player.getHandshakeState().equals(HandshakeState.AWAITING_REGISTRIES)) return;
                    if (player.isHandshakeCompleted()) {
                        completeHandshake(player);
                    }
                });
            }
        }
    }

    /**
     * Returns whether the given player is still connected.
     */
    protected boolean isConnected(@NotNull NoxesiumServerPlayer player) {
        return NoxesiumPlayerManager.getInstance().getPlayer(player.getUniqueId()) == player;
    }

    /**
     * Runs this task delayed by a tick.
     */
    protected abstract void runDelayed(Runnable runnable);
}
