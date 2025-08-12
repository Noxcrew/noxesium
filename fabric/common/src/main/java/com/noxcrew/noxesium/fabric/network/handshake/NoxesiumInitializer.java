package com.noxcrew.noxesium.fabric.network.handshake;

import com.noxcrew.noxesium.api.fabric.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.fabric.network.NoxesiumNetworking;
import com.noxcrew.noxesium.api.fabric.network.handshake.ClientboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.fabric.network.handshake.EntrypointProtocol;
import com.noxcrew.noxesium.api.fabric.network.handshake.HandshakePackets;
import com.noxcrew.noxesium.api.fabric.network.handshake.ServerboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.fabric.network.handshake.ServerboundHandshakePacket;
import com.noxcrew.noxesium.fabric.NoxesiumMod;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
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
 * Manages initialization of the Noxesium protocol.
 */
public class NoxesiumInitializer {
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
        // Register the handshake packet collection at all times
        HandshakePackets.INSTANCE.register();

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
        HandshakePackets.INSTANCE.CLIENTBOUND_HANDSHAKE_ACKNOWLEDGE.addListener(this, (ignored, packet, ignored3) -> {
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
        if (!NoxesiumNetworking.canSend(HandshakePackets.INSTANCE.SERVERBOUND_HANDSHAKE)) return;

        // Check if the connection has been established first, just in case
        if (Minecraft.getInstance().getConnection() == null) return;

        // Mark down that we are handshaking the connection
        state = HandshakeState.INITIAL_SERVER_REQUEST;
        challenges.clear();

        // Determine all entrypoints and their encrypted ids
        var ids = new HashMap<String, String>();
        NoxesiumMod.getInstance().getAllEntrypoints().forEach(entrypoint -> {
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
                    NoxesiumMod.getInstance().getLogger().error("Failed to encrypt entrypoint id {}", id);
                    return;
                }
            }

            // Fall back to no encryption!
            ids.put(id, secret);
        });

        // Inform the server about the handshake
        new ServerboundHandshakePacket(ids).send();
    }

    /**
     * Handles the server acknowledging the handshake packet.
     */
    private void handle(ClientboundHandshakeAcknowledgePacket packet) {
        var entrypoints = new HashSet<EntrypointProtocol>();
        for (var pair : packet.entrypoints().entrySet()) {
            var id = pair.getKey();
            var challengeResult = pair.getValue();

            // Determine which entrypoint this is
            var entrypoint = NoxesiumMod.getInstance().getEntrypoint(id);
            if (entrypoint == null) continue;

            // If the challenge result is invalid ignore this!
            if (!Objects.equals(challenges.get(entrypoint), challengeResult)) continue;

            // Initialize this entrypoint and add it to the list
            entrypoints.add(new EntrypointProtocol(
                    entrypoint.getId(), entrypoint.getProtocolVersion(), entrypoint.getRawVersion()));
            entrypoint.getAllFeatures().forEach(it -> NoxesiumMod.getInstance().registerFeature(it));
            entrypoint.getPacketCollections().forEach(it -> NoxesiumMod.getInstance()
                    .registerPackets(it));
        }

        // Inform the server about the handshake success
        new ServerboundHandshakeAcknowledgePacket(entrypoints).send();
    }

    /**
     * Un-initializes the connection with the server.
     */
    public void uninitialize() {
        if (state != HandshakeState.COMPLETE) return;
        state = HandshakeState.NONE;
        NoxesiumMod.getInstance().unregisterAll();
    }
}
