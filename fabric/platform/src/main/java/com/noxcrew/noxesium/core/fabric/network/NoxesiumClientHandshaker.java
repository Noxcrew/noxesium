package com.noxcrew.noxesium.core.fabric.network;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.EntrypointProtocol;
import com.noxcrew.noxesium.api.network.HandshakeState;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundRegistryIdentifiersPacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakePacket;
import com.noxcrew.noxesium.api.nms.NmsNoxesiumEntrypoint;
import com.noxcrew.noxesium.api.nms.NoxesiumNmsApi;
import com.noxcrew.noxesium.api.nms.network.HandshakePackets;
import com.noxcrew.noxesium.api.nms.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.api.registry.RegistryCollection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;

/**
 * Manages initialization of the Noxesium handshake protocol.
 */
public class NoxesiumClientHandshaker {
    /**
     * The current state of the server connection.
     */
    private HandshakeState state = HandshakeState.NONE;

    /**
     * All encryption challenges sent to the server awaiting an answer.
     */
    private final Map<NoxesiumEntrypoint, String> challenges = new HashMap<>();

    /**
     * Registers the initializer.
     */
    public void register() {
        // Every time the client joins a server we send over information on the version being used,
        // we initialize when both packets are known ad we are in the PLAY phase, whenever both have
        // happened.
        C2SPlayChannelEvents.REGISTER.register((ignored1, ignored2, ignored3, channels) -> {
            initialize();
        });
        ClientPlayConnectionEvents.JOIN.register((ignored1, ignored2, ignored3) -> {
            initialize();
        });

        // Call disconnection hooks
        ClientPlayConnectionEvents.DISCONNECT.register((ignored1, ignored2) -> {
            uninitialize();
        });

        ClientConfigurationConnectionEvents.START.register((ignored1, ignored2) -> {
            // Re-initialize when moving in/out of the config phase, we assume any server
            // running a proxy that doesn't use the configuration phase between servers
            // has their stuff set up well enough to remember the client's information.
            uninitialize();
        });

        // Listen to the server response to the handshake
        HandshakePackets.CLIENTBOUND_HANDSHAKE_ACKNOWLEDGE.addListener(this, (ignored, packet, ignored3) -> {
            handle(packet);
        });

        // Whenever we receive registry packets we update the registries
        HandshakePackets.CLIENTBOUND_REGISTRY_IDS.addListener(this, (ignored, packet, ignored3) -> {
            handle(packet);
        });
    }

    /**
     * Initializes the connection with the current server if the connection has been established.
     */
    public void initialize() {
        // Ignore if already initialized
        if (state != HandshakeState.NONE) return;

        // Don't allow if the server doesn't accept any handshakes
        if (!NoxesiumServerboundNetworking.getInstance().canSend(HandshakePackets.SERVERBOUND_HANDSHAKE)) return;

        // Check if the connection has been established first, just in case
        if (Minecraft.getInstance().getConnection() == null) return;

        // Register the handshake packet collection at all times
        HandshakePackets.INSTANCE.register();

        // Mark down that we are handshaking the connection
        state = HandshakeState.INITIAL_SERVER_REQUEST;
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
            var encryptionKeyFile = entrypoint.getEncryptionKey();
            if (encryptionKeyFile != null) {
                try {
                    try (var stream = encryptionKeyFile.openStream()) {
                        byte[] keyBytes = Base64.getDecoder().decode(stream.readAllBytes());
                        var keySpec = new SecretKeySpec(keyBytes, "AES");
                        var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
                        var encryptedId = Base64.getEncoder().encodeToString(cipher.doFinal(id.getBytes()));
                        var encryptedSecret = Base64.getEncoder().encodeToString(cipher.doFinal(secret.getBytes()));
                        ids.put(encryptedId, encryptedSecret);
                    }
                } catch (Exception x) {
                    NoxesiumApi.getLogger().error("Failed to encrypt entrypoint id {}", id);
                    return;
                }
            }

            // Fall back to no encryption!
            ids.put(id, secret);
        });

        // Inform the server about the handshake
        NoxesiumServerboundNetworking.send(new ServerboundHandshakePacket(ids));
    }

    /**
     * Handles the server acknowledging the handshake packet.
     */
    private void handle(ClientboundHandshakeAcknowledgePacket packet) {
        var api = NoxesiumApi.getInstance();
        var fabricApi = NoxesiumNmsApi.getInstance();
        var entrypoints = new ArrayList<EntrypointProtocol>();
        for (var pair : packet.entrypoints().entrySet()) {
            var id = pair.getKey();
            var challengeResult = pair.getValue();

            // Determine which entrypoint this is
            var entrypoint = api.getEntrypoint(id);
            if (entrypoint == null) continue;

            // If the challenge result is invalid ignore this!
            if (!Objects.equals(challenges.get(entrypoint), challengeResult)) continue;

            // Initialize this entrypoint and add it to the list
            entrypoints.add(new EntrypointProtocol(
                    entrypoint.getId(), entrypoint.getProtocolVersion(), entrypoint.getRawVersion()));
            entrypoint.getAllFeatures().forEach(api::registerFeature);
            entrypoint.getRegistryCollections().forEach(RegistryCollection::register);
            if (entrypoint instanceof NmsNoxesiumEntrypoint fabricEntrypoint) {
                fabricEntrypoint.getPacketCollections().forEach(fabricApi::registerPackets);
            }
        }

        // Inform the server about the handshake success
        NoxesiumServerboundNetworking.send(new ServerboundHandshakeAcknowledgePacket(entrypoints));
    }

    /**
     * Handles the server sending across registry contents.
     */
    private void handle(ClientboundRegistryIdentifiersPacket packet) {
        // TODO implement
        // TODO send back which ids were not found on the client so the server
        // knows to never send those!
    }

    /**
     * Un-initializes the connection with the server.
     */
    public void uninitialize() {
        if (state == HandshakeState.NONE) return;
        state = HandshakeState.NONE;
        HandshakePackets.INSTANCE.unregister();
        NoxesiumApi.getInstance().unregisterAll();
        NoxesiumNmsApi.getInstance().unregisterAll();
        Minecraft.getInstance().noxesium$clearComponents();
    }
}
