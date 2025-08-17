package com.noxcrew.noxesium.api.nms.network;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.HandshakeState;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundHandshakeCancelPacket;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundHandshakeCompletePacket;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundRegistryUpdatePacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakePacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundRegistryUpdateResultPacket;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.ServerNoxesiumRegistry;
import com.noxcrew.noxesium.api.util.EncryptionUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

/**
 * Performs handshaking with the client-side to be run from the server-side.
 */
public abstract class NoxesiumServerHandshaker {
    /**
     * The current state of the handshake with different clients.
     */
    protected final Map<UUID, HandshakeState> handshakeStates = new HashMap<>();

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

        // Respond to incoming handshake packets
        HandshakePackets.SERVERBOUND_HANDSHAKE.addListener(this, (reference, packet, player) -> {
            reference.handleHandshake(player, packet);
        });
        HandshakePackets.SERVERBOUND_HANDSHAKE_ACKNOWLEDGE.addListener(this, (reference, packet, player) -> {
            reference.handleHandshakeAcknowledge((ServerPlayer) player, packet);
        });
        HandshakePackets.SERVERBOUND_HANDSHAKE_CANCEL.addListener(this, (reference, packet, player) -> {
            reference.onPlayerDisconnect(player.getUUID());
        });
        HandshakePackets.SERVERBOUND_REGISTRY_UPDATE_RESULT.addListener(this, (reference, packet, player) -> {
            reference.onRegistryUpdateResult(player, packet);
        });
    }

    /**
     * Handles a client initiating a handshake with the server.
     */
    private void handleHandshake(@NotNull Player player, @NotNull ServerboundHandshakePacket packet) {
        if (handshakeStates.containsKey(player.getUUID())) {
            NoxesiumApi.getLogger()
                    .error(
                            "Received registry contents while in '{}' state, destroying connection!",
                            handshakeStates.getOrDefault(player.getUUID(), HandshakeState.NONE));
            destroy(player);
            return;
        }

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
            destroy(player);
            return;
        }

        // Determine the response based on the current state
        var acknowledgePacket = new ClientboundHandshakeAcknowledgePacket(entrypoints);
        if (NoxesiumClientboundNetworking.getInstance()
                .canReceive(player, HandshakePackets.CLIENTBOUND_HANDSHAKE_ACKNOWLEDGE)) {
            // The client has already indicated it can receive the acknowledgment packet,
            // send it immediately!
            NoxesiumClientboundNetworking.send(player, acknowledgePacket);
            handshakeStates.put(player.getUUID(), HandshakeState.AWAITING_RESPONSE);
        } else {
            // The client hasn't sent that it can receive the acknowledgment packet yet, so
            // we put the packet in a pending list and wait for it.
            pendingPackets.put(player.getUUID(), acknowledgePacket);
            handshakeStates.put(player.getUUID(), HandshakeState.NONE);
        }
    }

    /**
     * Handle a successful handshake acknowledgement, register all packets, and call events.
     */
    private void handleHandshakeAcknowledge(
            @NotNull ServerPlayer player, @NotNull ServerboundHandshakeAcknowledgePacket packet) {
        if (!Objects.equals(handshakeStates.get(player.getUUID()), HandshakeState.AWAITING_RESPONSE)) {
            NoxesiumApi.getLogger()
                    .error(
                            "Received registry contents while in '{}' state, destroying connection!",
                            handshakeStates.getOrDefault(player.getUUID(), HandshakeState.NONE));
            destroy(player);
            return;
        }

        // Mark that we are now waiting to sync registries
        handshakeStates.put(player.getUUID(), HandshakeState.AWAITING_REGISTRIES);

        // Store this player in the player manager and start updating their object
        var noxesiumPlayer = new NoxesiumServerPlayer(player, packet.protocols());
        NoxesiumPlayerManager.getInstance().registerPlayer(player.getUUID(), noxesiumPlayer);

        // Start tasks for sending registries and receiving registration of plugin channels
        var entrypoints = noxesiumPlayer.getSupportedEntrypointIds();
        for (var registry : NoxesiumRegistries.REGISTRIES) {
            // We only have to synchronize server registries as only sided registries
            // use indices which have to be synchronized.
            if (registry instanceof ServerNoxesiumRegistry<?> serverRegistry) {
                // Ignore empty registries!
                var syncContents = serverRegistry.determineSyncableContents(entrypoints);
                if (syncContents.isEmpty()) continue;

                var id = registryUpdateIdentifier.getAndIncrement();
                noxesiumPlayer.awaitRegistrySync(id);
                NoxesiumClientboundNetworking.send(
                        player,
                        new ClientboundRegistryUpdatePacket(
                                id, serverRegistry.id(), syncContents));
            }
        }

        // Test if all tasks are already complete
        if (noxesiumPlayer.isHandshakeCompleted()) {
            completeHandshake(noxesiumPlayer);
        }
    }

    /**
     * Handles a registry update acknowledgement.
     */
    private void onRegistryUpdateResult(@NotNull Player player, @NotNull ServerboundRegistryUpdateResultPacket packet) {
        if (!Objects.equals(handshakeStates.get(player.getUUID()), HandshakeState.AWAITING_REGISTRIES)) {
            NoxesiumApi.getLogger()
                    .error(
                            "Received registry update result while in '{}' state, destroying connection!",
                            handshakeStates.getOrDefault(player.getUUID(), HandshakeState.NONE));
            destroy(player);
            return;
        }

        var noxesiumPlayer = NoxesiumPlayerManager.getInstance().getPlayer(player.getUUID());
        if (noxesiumPlayer == null) {
            NoxesiumApi.getLogger()
                    .error("No player registered when receiving registry update result, destroying connection!");
            destroy(player);
            return;
        }

        if (noxesiumPlayer.acknowledgeRegistrySync(packet.id())) {
            // Try to complete the handshaking
            if (noxesiumPlayer.isHandshakeCompleted()) {
                completeHandshake(noxesiumPlayer);
            }
        } else {
            NoxesiumApi.getLogger()
                    .error("Received invalid registry result update for id {}, destroying connection!", packet.id());
            destroy(player);
        }
    }

    /**
     * Destroys the connection with the given player.
     */
    private void destroy(@NotNull Player player) {
        NoxesiumClientboundNetworking.send(player, new ClientboundHandshakeCancelPacket());
        onPlayerDisconnect(player.getUUID());
    }

    /**
     * Completes the handshake for the given player.
     */
    protected boolean completeHandshake(@NotNull NoxesiumServerPlayer noxesiumPlayer) {
        if (!Objects.equals(handshakeStates.get(noxesiumPlayer.getUniqueId()), HandshakeState.AWAITING_REGISTRIES)) {
            NoxesiumApi.getLogger()
                    .error(
                            "Tried to complete handshake while in '{}' state, destroying connection!",
                            handshakeStates.getOrDefault(noxesiumPlayer.getUniqueId(), HandshakeState.NONE));
            destroy(noxesiumPlayer.getNmsPlayer());
            return false;
        }

        // Move to the last handshake state and send the client a completion message
        handshakeStates.put(noxesiumPlayer.getUniqueId(), HandshakeState.COMPLETE);
        NoxesiumClientboundNetworking.send(noxesiumPlayer.getNmsPlayer(), new ClientboundHandshakeCompletePacket());
        NoxesiumApi.getLogger()
                .info(
                        "Authenticated {} on Noxesium {} with {} entrypoints",
                        noxesiumPlayer.getNmsPlayer().getGameProfile().getName(),
                        noxesiumPlayer.getBaseVersion(),
                        noxesiumPlayer.getSupportedEntrypoints().size());
        return true;
    }

    /**
     * Handles a player disconnecting or cancelling the handshake.
     */
    public void onPlayerDisconnect(UUID uuid) {
        handshakeStates.remove(uuid);
        pendingPackets.remove(uuid);
        pendingChecks.remove(uuid);
        NoxesiumPlayerManager.getInstance().unregisterPlayer(uuid);
    }

    /**
     * Handles the given channel being registered for player, sending any pending
     * packets that were waiting for the channel to be registered.
     */
    public void onChannelRegistered(@NotNull ServerPlayer player, @NotNull String channel) {
        if (channel.equals(
                HandshakePackets.CLIENTBOUND_HANDSHAKE_ACKNOWLEDGE.id().toString())) {
            // Delay by a tick so the other handshake channels are also registered!
            runDelayed(() -> {
                if (player.hasDisconnected()) return;

                // The client has requested the handshake, but it has not yet been
                // sent the acknowledgement, send the pending packet now that we
                // definitely can!
                var packet = pendingPackets.remove(player.getUUID());
                if (packet == null) return;
                NoxesiumClientboundNetworking.send(player, packet);
                handshakeStates.put(player.getUUID(), HandshakeState.AWAITING_RESPONSE);
            });
        } else {
            // Check if the handshake should complete yet based on new channels being registered!
            if (Objects.equals(handshakeStates.get(player.getUUID()), HandshakeState.AWAITING_REGISTRIES)) {
                // Prevent tasks from starting too often if we register a whole group of channels!
                if (pendingChecks.contains(player.getUUID())) return;
                pendingChecks.add(player.getUUID());

                runDelayed(() -> {
                    pendingChecks.remove(player.getUUID());

                    if (player.hasDisconnected()) return;
                    if (!Objects.equals(handshakeStates.get(player.getUUID()), HandshakeState.AWAITING_REGISTRIES))
                        return;

                    var noxesiumPlayer = NoxesiumPlayerManager.getInstance().getPlayer(player.getUUID());
                    if (noxesiumPlayer == null) return;
                    if (noxesiumPlayer.isHandshakeCompleted()) {
                        completeHandshake(noxesiumPlayer);
                    }
                });
            }
        }
    }

    /**
     * Runs this task delayed by a tick.
     */
    public abstract void runDelayed(Runnable runnable);
}
