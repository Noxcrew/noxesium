package com.noxcrew.noxesium.api.network.handshake;

import com.noxcrew.noxesium.api.ClientNoxesiumEntrypoint;
import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.api.network.ConnectionProtocolType;
import com.noxcrew.noxesium.api.network.EntrypointProtocol;
import com.noxcrew.noxesium.api.network.ModInfo;
import com.noxcrew.noxesium.api.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundLazyPacketsPacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundRegistryContentUpdatePacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundRegistryIdsUpdatePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakeCancelPacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakePacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundLazyPacketsPacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundRegistryUpdateResultPacket;
import com.noxcrew.noxesium.api.registry.ClientNoxesiumRegistry;
import com.noxcrew.noxesium.api.registry.GameComponents;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry;
import com.noxcrew.noxesium.api.util.EncryptionUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.key.Key;

/**
 * Performs handshaking with the server-side to be run from the client-side.
 */
public abstract class NoxesiumClientHandshaker {
    /**
     * The current state of the server connection.
     */
    protected HandshakeState state = HandshakeState.NONE;

    /**
     * An amount of ticks before the client should re-attempt to start a handshake.
     */
    protected int handshakeCooldown = -1;

    /**
     * All encryption challenges sent to the server awaiting an answer.
     */
    protected final Map<NoxesiumEntrypoint, String> challenges = new HashMap<>();

    /**
     * Returns the current handshaking state.
     */
    public HandshakeState getHandshakeState() {
        return state;
    }

    /**
     * Registers the handshaker.
     */
    public void register() {
        // Listen to the server response to the handshake
        HandshakePackets.CLIENTBOUND_HANDSHAKE_ACKNOWLEDGE.addListener(this, (reference, packet, ignored3) -> {
            reference.handleHandshakeAcknowledge(packet);
        });

        // Whenever we receive registry packets we update the registries
        HandshakePackets.CLIENTBOUND_REGISTRY_IDS_UPDATE.addListener(this, (reference, packet, ignored3) -> {
            reference.handleRegistryUpdate(packet);
        });
        HandshakePackets.CLIENTBOUND_REGISTRY_CONTENT_UPDATE.addListener(this, (reference, packet, ignored3) -> {
            reference.handleRegistryUpdate(packet);
        });

        // Whenever the server indicates a transfer we update the state
        HandshakePackets.CLIENTBOUND_HANDSHAKE_TRANSFERRED.addListener(this, (reference, packet, ignored3) -> {
            reference.handleTransfer();
        });

        // Whenever the handshake is completed we listen.
        HandshakePackets.CLIENTBOUND_HANDSHAKE_COMPLETE.addListener(this, (reference, packet, ignored3) -> {
            reference.handleComplete();
        });

        // Handle a new set of lazy packets being enabled.
        HandshakePackets.CLIENTBOUND_LAZY_PACKETS.addListener(this, (reference, packet, ignored3) -> {
            reference.handleLazyPackets(packet);
        });

        // Whenever the handshake is interrupted.
        HandshakePackets.CLIENTBOUND_HANDSHAKE_CANCEL.addListener(this, (reference, packet, ignored3) -> {
            // End the handshake without sending a packet about it!
            reference.uninitialize(true);
        });
    }

    /**
     * Ticks the client, attempting to start a new handshake when relevant.
     */
    public void tick() {
        if (handshakeCooldown < 0) return;
        if (handshakeCooldown-- <= 0) {
            initialize();
        }
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
    protected void handleHandshakeAcknowledge(ClientboundHandshakeAcknowledgePacket packet) {
        if (state != HandshakeState.AWAITING_RESPONSE) {
            NoxesiumApi.getLogger()
                    .error("Received handshake response while in '{}' state, destroying connection!", state);
            uninitialize();
            return;
        }

        var api = NoxesiumApi.getInstance();
        var entrypoints = new ArrayList<EntrypointProtocol>();
        var enabledLazyPackets = new HashSet<Key>();
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
                        entrypoint.getId(), clientEntrypoint.getVersion(), clientEntrypoint.getCapabilities());
                entrypoints.add(protocol);
                api.activateEntrypoint(protocol);

                // Go through all packets for this entry point and determine if they are lazy but enabled
                for (var packetCollection : clientEntrypoint.getPacketCollections()) {
                    for (var packetType : packetCollection.getPackets()) {
                        if (!packetType.lazy) continue;
                        if (packetType.hasListeners()) {
                            enabledLazyPackets.add(packetType.id());
                        }
                    }
                }
            }
        }

        // Break the handshake if no entrypoints were properly established
        if (entrypoints.isEmpty()) {
            NoxesiumApi.getLogger().error("Server sent no valid entrypoints for authentication");
            uninitialize();
            return;
        }

        // Inform the server about the handshake success and start waiting for registries
        NoxesiumServerboundNetworking.send(
                new ServerboundHandshakeAcknowledgePacket(entrypoints, collectMods(entrypoints)));
        state = HandshakeState.AWAITING_REGISTRIES;

        // Inform the server which packets are lazy
        if (!enabledLazyPackets.isEmpty()) {
            NoxesiumServerboundNetworking.send(new ServerboundLazyPacketsPacket(enabledLazyPackets));
        }
    }

    /**
     * Collects all installed mods to be sent to the server given the list of entrypoints which were
     * successfully authenticated.
     */
    protected abstract Collection<ModInfo> collectMods(List<EntrypointProtocol> entrypoints);

    /**
     * Handles the server sending across registry ids.
     */
    protected void handleRegistryUpdate(ClientboundRegistryIdsUpdatePacket packet) {
        if (state != HandshakeState.AWAITING_REGISTRIES) {
            NoxesiumApi.getLogger().error("Received registry ids while in '{}' state, destroying connection!", state);
            uninitialize();
            return;
        }

        // Process all contents of the packet
        var unknownKeys = new ArrayList<Integer>();
        var registry = (ClientNoxesiumRegistry<?>) NoxesiumRegistries.REGISTRIES_BY_ID.get(packet.registry());
        if (registry == null) {
            NoxesiumApi.getLogger()
                    .error(
                            "Received registry ids for registry '{}' which does not exist",
                            packet.registry().asString());
            uninitialize();
            return;
        }
        if (packet.reset()) {
            registry.resetMappings();
        }
        for (var entry : packet.ids().entrySet()) {
            if (!registry.registerMapping(entry.getKey(), entry.getValue())) {
                unknownKeys.add(entry.getValue());
            }
        }

        // Inform the server which keys were not known to the client
        NoxesiumServerboundNetworking.send(new ServerboundRegistryUpdateResultPacket(packet.id(), unknownKeys));
    }

    /**
     * Handles the server sending across registry contents.
     */
    protected void handleRegistryUpdate(ClientboundRegistryContentUpdatePacket packet) {
        if (state != HandshakeState.AWAITING_REGISTRIES) {
            NoxesiumApi.getLogger()
                    .error("Received registry contents while in '{}' state, destroying connection!", state);
            uninitialize();
            return;
        }

        // Process all contents of the packet
        var patch = packet.patch();
        var registry = (ClientNoxesiumRegistry<?>) NoxesiumRegistries.REGISTRIES_BY_ID.get(patch.getRegistry());
        if (registry == null) {
            NoxesiumApi.getLogger()
                    .error(
                            "Received registry contents for registry '{}' which does not exist",
                            patch.getRegistry().asString());
            uninitialize();
            return;
        }

        // Reset the entire registry first if requested
        if (packet.reset()) {
            registry.reset();
        }

        for (var entry : patch.getMap().entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();

            if (value.isEmpty()) {
                registry.remove(key);
            } else {
                var id = patch.getKeys().get(key);
                registry.registerAny(key, id, value);
            }
        }

        // Inform the server that the registry was received
        NoxesiumServerboundNetworking.send(new ServerboundRegistryUpdateResultPacket(packet.id(), List.of()));
    }

    /**
     * Handle the server completing the handshake process.
     */
    protected void handleComplete() {
        if (state != HandshakeState.AWAITING_REGISTRIES) {
            NoxesiumApi.getLogger()
                    .error("Received handshake completion while in '{}' state, destroying connection!", state);
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
     * Handles the server sending an update on lazy packets.
     */
    protected void handleLazyPackets(ClientboundLazyPacketsPacket packet) {
        if (state == HandshakeState.NONE) {
            // Don't allow this packet unless we have started the handshake
            uninitialize();
            return;
        }
        NoxesiumServerboundNetworking.getInstance().addEnabledLazyPackets(packet.packets());
    }

    /**
     * Handles the server informing the client about a transfer.
     */
    protected void handleTransfer() {
        if (state != HandshakeState.COMPLETE) {
            // Whenever we got transferred while not completely done handshaking we
            // trigger an immediate handshake failure and re-attempt handshaking soon.
            uninitialize();
            return;
        }

        // Set the state back to awaiting registries so the server can re-send
        // all registry information!
        state = HandshakeState.AWAITING_REGISTRIES;

        // Clear out any data that the previous server set which the
        // new server does not know about.
        resetLocalCaches();
    }

    /**
     * Resets any data on the client set based on server information that isn't
     * tied to the current world or entities.
     */
    protected void resetLocalCaches() {
        challenges.clear();
        NoxesiumApi.getInstance().getAllFeatures().forEach(NoxesiumFeature::onTransfer);
        GameComponents.getInstance().noxesium$reloadComponents();
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
        if (!recursive) {
            // Stop any handshake attempts from any non-recursive cancellation
            handshakeCooldown = -1;

            // Start by sending a packet to inform the server the handshake was cancelled!
            NoxesiumServerboundNetworking.send(new ServerboundHandshakeCancelPacket());
        } else {
            // Attempt another handshake in 10 seconds!
            handshakeCooldown = 200;
        }

        // Don't proceed unless there is a handshake to cancel
        if (state == HandshakeState.NONE) return;

        state = HandshakeState.NONE;
        challenges.clear();
        NoxesiumServerboundNetworking.getInstance().setConfiguredProtocol(ConnectionProtocolType.NONE);
        NoxesiumServerboundNetworking.getInstance().resetEnablesLazyPackets();
        HandshakePackets.INSTANCE.unregister();
        NoxesiumApi.getInstance().unregisterAll();
        GameComponents.getInstance().noxesium$reloadComponents();
    }
}
