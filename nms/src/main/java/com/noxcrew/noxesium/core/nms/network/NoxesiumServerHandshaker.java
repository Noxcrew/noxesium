package com.noxcrew.noxesium.core.nms.network;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.HandshakeState;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundHandshakeCancelPacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakePacket;
import com.noxcrew.noxesium.api.nms.network.HandshakePackets;
import com.noxcrew.noxesium.api.nms.network.NoxesiumClientboundNetworking;
import com.noxcrew.noxesium.api.util.EncryptionUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
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
            reference.handleHandshakeAcknowledge(player, packet);
        });
        HandshakePackets.SERVERBOUND_HANDSHAKE_CANCEL.addListener(this, (reference, packet, player) -> {
            reference.onPlayerDisconnect(player.getUUID());
        });
    }

    /**
     * Handles a client initiating a handshake with the server.
     */
    private void handleHandshake(@NotNull Player player, @NotNull ServerboundHandshakePacket packet) {
        if (handshakeStates.containsKey(player.getUUID())) {
            NoxesiumApi.getLogger()
                    .error(
                            "Received Noxesium registry contents while in '{}' state, destroying connection!",
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
            @NotNull Player player, @NotNull ServerboundHandshakeAcknowledgePacket packet) {
        if (!Objects.equals(handshakeStates.get(player.getUUID()), HandshakeState.AWAITING_RESPONSE)) {
            NoxesiumApi.getLogger()
                    .error(
                            "Received Noxesium registry contents while in '{}' state, destroying connection!",
                            handshakeStates.getOrDefault(player.getUUID(), HandshakeState.NONE));
            destroy(player);
            return;
        }

        // Store this player in the player manager and start updating their object
        NoxesiumPlayerManager.getInstance()
                .registerPlayer(player.getUUID(), new NoxesiumServerPlayer(player.getUUID(), packet.protocols()));

        // Start tasks for sending registries and receiving registration of
    }

    /**
     * Destroys the connection with the given player.
     */
    public void destroy(@NotNull Player player) {
        NoxesiumClientboundNetworking.send(player, new ClientboundHandshakeCancelPacket());
        onPlayerDisconnect(player.getUUID());
    }

    /**
     * Handles a player disconnecting or cancelling the handshake.
     */
    public void onPlayerDisconnect(UUID uuid) {
        handshakeStates.remove(uuid);
        pendingPackets.remove(uuid);
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
        }
    }

    /**
     * Runs this task delayed by a tick.
     */
    public abstract void runDelayed(Runnable runnable);
}
