package com.noxcrew.noxesium.api.network.handshake;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.NoxesiumClientboundNetworking;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeCancelPacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeCompletePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeTransferredPacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundRegistryContentUpdatePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundRegistryIdsUpdatePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundRegistryUpdateResultPacket;
import com.noxcrew.noxesium.api.player.IdChangeSet;
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
     * Pending players which are awaiting the transfer packet being registered.
     */
    protected final Set<UUID> pendingTransfers = new HashSet<>();

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
                var syncContents = registry.determineAllChangedContent(entrypoints);
                if (syncContents.isEmpty()) continue;

                // Mark down that we're waiting on this registry sync!
                var id = registryUpdateIdentifier.getAndIncrement();
                var added = new HashSet<Integer>();
                var removed = new HashSet<Integer>();
                for (var entry : syncContents.getMap().entrySet()) {
                    var keyId = syncContents.getKeys().get(entry.getKey());
                    if (entry.getValue().isEmpty()) {
                        removed.add(keyId);
                    } else {
                        added.add(keyId);
                    }
                }
                player.awaitRegistrySync(id, new IdChangeSet(registry, false, added, removed));
                player.sendPacket(new ClientboundRegistryContentUpdatePacket(id, false, syncContents));
            }
        }

        // Mark all registries as non-dirty
        dirtyRegistries.forEach(SynchronizedServerNoxesiumRegistry::clearPendingUpdates);
    }

    /**
     * Handles a client transferring to this server.
     */
    protected void handleTransfer(@NotNull NoxesiumServerPlayer player) {
        if (NoxesiumPlayerManager.getInstance().getPlayer(player.getUniqueId()) != null) {
            NoxesiumApi.getLogger()
                    .error("Failed to send transfer user as data was already present, destroying connection!");
            destroy(player.getUniqueId());
            return;
        }

        // If the handshake start is successful we register the player instance
        NoxesiumPlayerManager.getInstance().registerPlayer(player.getUniqueId(), player);

        if (NoxesiumClientboundNetworking.getInstance()
                .canReceive(player, ClientboundHandshakeTransferredPacket.class)) {
            // The client has already indicated it can receive the acknowledgment packet,
            // send it immediately!
            if (!player.sendPacket(new ClientboundHandshakeTransferredPacket())) {
                NoxesiumApi.getLogger().error("Failed to send handshake transfer packet, destroying connection!");
                destroy(player.getUniqueId());
                return;
            }
            completeTransfer(player);
        } else {
            // The client hasn't sent that it can receive the transfer packet,
            // so we queue up this transfer.
            pendingTransfers.add(player.getUniqueId());
        }
    }

    /**
     * Completes the transfer and starts synchronizing the new registries.
     */
    protected void completeTransfer(@NotNull NoxesiumServerPlayer player) {
        if (!player.getHandshakeState().equals(HandshakeState.NONE)) {
            NoxesiumApi.getLogger()
                    .error(
                            "Tried to complete transfer while in '{}' state, destroying connection!",
                            player.getHandshakeState());
            destroy(player.getUniqueId());
            return;
        }

        // Wait for registries to complete synchronizing
        player.setHandshakeState(HandshakeState.AWAITING_REGISTRIES);

        // Re-send the player all registries, we do track the indices across stored data
        // but the registry contents may not be the same between servers so we re-sync
        // for safety. There is an option in the future to add a partial syncing protocol
        // that only syncs changes, but for now we redo it all.
        synchronizeRegistries(player.getUniqueId());
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
            if (!player.sendPacket(acknowledgePacket)) {
                NoxesiumApi.getLogger()
                        .error("Failed to send handshake acknowledgement packet, destroying connection!");
                destroy(player.getUniqueId());
                return;
            }
            player.setHandshakeState(HandshakeState.AWAITING_RESPONSE);
        } else {
            // The client hasn't sent that it can receive the acknowledgment packet yet, so
            // we put the packet in a pending list and wait for it.
            pendingPackets.put(player.getUniqueId(), acknowledgePacket);
            player.setHandshakeState(HandshakeState.NONE);
        }
    }

    /**
     * Resynchronizes contents of synchronizable registries with a player.
     */
    protected void synchronizeRegistries(@NotNull UUID uniqueId) {
        var player = NoxesiumPlayerManager.getInstance().getPlayer(uniqueId);
        if (player == null) {
            NoxesiumApi.getLogger().error("Asked to synchronize registries with unknown player!");
            destroy(uniqueId);
            return;
        }

        if (!player.getHandshakeState().equals(HandshakeState.AWAITING_REGISTRIES)
                && !player.getHandshakeState().equals(HandshakeState.COMPLETE)) {
            NoxesiumApi.getLogger()
                    .error(
                            "Asked to re-synchronize registries while in '{}' state, destroying connection!",
                            player.getHandshakeState());
            destroy(uniqueId);
            return;
        }

        var entrypoints = player.getSupportedEntrypointIds();
        for (var registry : NoxesiumRegistries.REGISTRIES) {
            // We only have to synchronize server registries as only sided registries
            // use indices which have to be synchronized.
            if (registry instanceof SynchronizedServerNoxesiumRegistry<?> synchronizedRegistry) {
                // Ignore empty registries!
                var syncContents = synchronizedRegistry.determineAllSyncableContent(entrypoints);
                if (syncContents.isEmpty()) continue;

                var id = registryUpdateIdentifier.getAndIncrement();
                player.awaitRegistrySync(id, new IdChangeSet(registry, true, syncContents.getIds(), Set.of()));
                if (!player.sendPacket(new ClientboundRegistryContentUpdatePacket(id, true, syncContents))) {
                    NoxesiumApi.getLogger()
                            .error("Failed to send registry contents update packet, destroying connection!");
                    destroy(uniqueId);
                    return;
                }
            } else if (registry instanceof ServerNoxesiumRegistry<?> serverRegistry) {
                // Ignore empty registries!
                var syncContents = serverRegistry.determineAllSyncableIds(entrypoints);
                if (syncContents.isEmpty()) continue;

                var id = registryUpdateIdentifier.getAndIncrement();
                player.awaitRegistrySync(id, new IdChangeSet(registry, true, syncContents.values(), Set.of()));
                if (!player.sendPacket(
                        new ClientboundRegistryIdsUpdatePacket(id, true, serverRegistry.id(), syncContents))) {
                    NoxesiumApi.getLogger().error("Failed to send registry id update packet, destroying connection!");
                    destroy(uniqueId);
                    return;
                }
            }
        }

        // Test if all tasks are already complete
        if (player.isHandshakeCompleted()) {
            completeHandshake(player);
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
        synchronizeRegistries(uniqueId);
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

        if (player.acknowledgeRegistrySync(packet.id(), packet.unknownKeys())) {
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
        if (!player.isTransfer() && !player.sendPacket(new ClientboundHandshakeCompletePacket())) {
            NoxesiumApi.getLogger().error("Failed to send handshake completion packet, destroying connection!");
            destroy(player.getUniqueId());
            return false;
        }
        NoxesiumApi.getLogger()
                .info(
                        "Authenticated {} on Noxesium {} with {} entrypoints: {}",
                        player.getUsername(),
                        player.getBaseVersion(),
                        player.getSupportedEntrypoints().size(),
                        player.getSupportedEntrypointIds());
        return true;
    }

    /**
     * Handles a player disconnecting or cancelling the handshake.
     */
    protected void onPlayerDisconnect(UUID uuid) {
        pendingPackets.remove(uuid);
        pendingTransfers.remove(uuid);
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
        } else if (channel.equals(
                HandshakePackets.CLIENTBOUND_HANDSHAKE_TRANSFERRED.id().toString())) {
            // Delay by a tick so the other handshake channels are also registered!
            runDelayed(() -> {
                if (!isConnected(player)) return;

                // The client is waiting for the transfer packet to be registered,
                // finish the transfer after it.
                if (!pendingTransfers.remove(player.getUniqueId())) return;
                completeTransfer(player);
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
