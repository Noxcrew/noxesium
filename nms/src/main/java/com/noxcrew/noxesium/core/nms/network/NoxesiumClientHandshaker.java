package com.noxcrew.noxesium.core.nms.network;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.EntrypointProtocol;
import com.noxcrew.noxesium.api.network.HandshakeState;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundHandshakeCompletePacket;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundRegistryIdentifiersPacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakeCancelPacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakePacket;
import com.noxcrew.noxesium.api.nms.ClientNoxesiumEntrypoint;
import com.noxcrew.noxesium.api.nms.NoxesiumNmsApi;
import com.noxcrew.noxesium.api.nms.network.HandshakePackets;
import com.noxcrew.noxesium.api.nms.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.api.registry.GameComponents;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry;
import com.noxcrew.noxesium.api.util.EncryptionUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Performs handshaking with the server-side to be run from the client-side.
 */
public abstract class NoxesiumClientHandshaker {
    /**
     * The current state of the server connection.
     */
    protected HandshakeState state = HandshakeState.NONE;

    /**
     * All encryption challenges sent to the server awaiting an answer.
     */
    protected final Map<NoxesiumEntrypoint, String> challenges = new HashMap<>();

    /**
     * Registers the handshaker.
     */
    public void register() {
        // Listen to the server response to the handshake
        HandshakePackets.CLIENTBOUND_HANDSHAKE_ACKNOWLEDGE.addListener(this, (reference, packet, ignored3) -> {
            reference.handle(packet);
        });

        // Whenever we receive registry packets we update the registries
        HandshakePackets.CLIENTBOUND_REGISTRY_IDS.addListener(this, (reference, packet, ignored3) -> {
            reference.handle(packet);
        });

        // Whenever the handshake is completed we listen.
        HandshakePackets.CLIENTBOUND_HANDSHAKE_COMPLETE.addListener(this, (reference, packet, ignored3) -> {
            reference.handle(packet);
        });

        // Whenever the handshake is interrupted.
        HandshakePackets.CLIENTBOUND_HANDSHAKE_CANCEL.addListener(this, (reference, packet, ignored3) -> {
            // End the handshake without sending a packet about it!
            reference.uninitialize(true);
        });
    }

    /**
     * Initializes the connection with the current server if the connection has been established.
     */
    public void initialize() {
        // Ignore if already initialized since this is driven by the client itself
        if (state != HandshakeState.NONE) return;

        // Register the handshake packet collection at all times
        HandshakePackets.INSTANCE.register(null);

        // Mark down that we are handshaking the connection
        state = HandshakeState.AWAITING_RESPONSE;
        challenges.clear();

        // Determine all entrypoints and their encrypted ids
        var ids = new HashMap<String, String>();
        NoxesiumApi.getInstance().getAllEntrypoints().forEach(entrypoint -> {
            // Determine a secret UUID for every entrypoint that we send to the server
            // as encrypted and expect it to send back unencrypted!
            var secret = UUID.randomUUID().toString();
            challenges.put(entrypoint, secret);

            // Attempt to encrypt this id and secret
            var id = entrypoint.getId();
            var encryptedValues = EncryptionUtil.encrypt(entrypoint.getEncryptionKey(), List.of(id, secret));
            ids.put(encryptedValues.getFirst(), encryptedValues.getLast());
        });

        // Inform the server about the handshake
        NoxesiumServerboundNetworking.send(new ServerboundHandshakePacket(ids));
    }

    /**
     * Handles the server acknowledging the handshake packet.
     */
    protected void handle(ClientboundHandshakeAcknowledgePacket packet) {
        if (state != HandshakeState.AWAITING_RESPONSE) {
            NoxesiumApi.getLogger()
                    .error("Received Noxesium handshake response while in '{}' state, destroying connection!", state);
            uninitialize();
            return;
        }

        var api = NoxesiumApi.getInstance();
        var fabricApi = NoxesiumNmsApi.getInstance();
        var entrypoints = new ArrayList<EntrypointProtocol>();
        for (var pair : packet.entrypoints().entrySet()) {
            var id = pair.getKey();
            var challengeResult = pair.getValue();

            // Determine which entrypoint this is
            var entrypoint = api.getEntrypoint(id);
            if (entrypoint == null) continue;

            // If the challenge result is invalid, fail the handshake!
            if (!Objects.equals(challenges.get(entrypoint), challengeResult)) {
                NoxesiumApi.getLogger().error("Server responded with invalid decryption for entrypoint id {}", id);
                uninitialize();
                return;
            }

            // Initialize this entrypoint and add it to the list
            if (entrypoint instanceof ClientNoxesiumEntrypoint clientEntrypoint) {
                var protocol = new EntrypointProtocol(
                        entrypoint.getId(), clientEntrypoint.getProtocolVersion(), clientEntrypoint.getRawVersion());
                entrypoints.add(protocol);
                api.activateEntrypoint(protocol);
                clientEntrypoint.getPacketCollections().forEach(it -> fabricApi.registerPackets(entrypoint, it));
            }
        }

        // Break the handshake if no entrypoints were properly established
        if (entrypoints.isEmpty()) {
            NoxesiumApi.getLogger().error("Server sent no valid entrypoints for authentication");
            uninitialize();
            return;
        }

        // Inform the server about the handshake success and start waiting for registries
        NoxesiumServerboundNetworking.send(new ServerboundHandshakeAcknowledgePacket(entrypoints));
        state = HandshakeState.AWAITING_REGISTRIES;
    }

    /**
     * Handles the server sending across registry contents.
     */
    protected void handle(ClientboundRegistryIdentifiersPacket packet) {
        if (state != HandshakeState.AWAITING_REGISTRIES) {
            NoxesiumApi.getLogger()
                    .error("Received Noxesium registry contents while in '{}' state, destroying connection!", state);
            uninitialize();
            return;
        }

        // TODO send back which ids were not found on the client so the server
        // knows to never send those!
    }

    /**
     * Handle the server completing the handshake process.
     */
    protected void handle(ClientboundHandshakeCompletePacket packet) {
        if (state != HandshakeState.AWAITING_REGISTRIES) {
            NoxesiumApi.getLogger()
                    .error("Received Noxesium handshake completion while in '{}' state, destroying connection!", state);
            uninitialize();
            return;
        }

        // Mark how many entrypoints were successfully activated
        NoxesiumApi.getLogger()
                .info(
                        "Successfully authenticated {} out of {} entrypoints with joined server",
                        NoxesiumApi.getInstance().getActiveEntrypoints().size(),
                        NoxesiumApi.getInstance().getAllEntrypoints().size());

        // Note how many registry entries were read
        NoxesiumApi.getLogger()
                .info(
                        "Successfully registered {} entries in {} registries from joined server",
                        NoxesiumRegistries.REGISTRIES.stream()
                                .mapToInt(NoxesiumRegistry::size)
                                .sum(),
                        NoxesiumRegistries.REGISTRIES.size());

        // Mark the handshaking complete
        state = HandshakeState.COMPLETE;

        // Register all features for the successfully authenticated entrypoints so features can
        // start sending packets properly on initialization like client settings
        var api = NoxesiumApi.getInstance();
        NoxesiumApi.getInstance().getActiveEntrypoints().forEach(entrypoint -> {
            entrypoint.getAllFeatures().forEach(api::registerFeature);
        });
    }

    /**
     * Un-initializes the connection with the server.
     */
    public void uninitialize() {
        uninitialize(false);
    }

    /**
     * Un-initializes the connection with the server.
     */
    public void uninitialize(boolean recursive) {
        if (state == HandshakeState.NONE) return;

        // Start by sending a packet to inform the server the handshake was cancelled!
        if (!recursive) NoxesiumServerboundNetworking.send(new ServerboundHandshakeCancelPacket());

        state = HandshakeState.NONE;
        challenges.clear();
        HandshakePackets.INSTANCE.unregister();
        NoxesiumApi.getInstance().unregisterAll();
        NoxesiumNmsApi.getInstance().unregisterAll();
        GameComponents.getInstance().noxesium$reloadComponents();
    }
}
