package com.noxcrew.noxesium.core.fabric.network;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.EntrypointProtocol;
import com.noxcrew.noxesium.api.network.HandshakeState;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundRegistryIdentifiersPacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakeAcknowledgePacket;
import com.noxcrew.noxesium.api.network.serverbound.ServerboundHandshakePacket;
import com.noxcrew.noxesium.api.nms.ClientNoxesiumEntrypoint;
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
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import kotlin.text.Charsets;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
        HandshakePackets.CLIENTBOUND_HANDSHAKE_ACKNOWLEDGE.addListener(this, (reference, packet, ignored3) -> {
            reference.handle(packet);
        });

        // Whenever we receive registry packets we update the registries
        HandshakePackets.CLIENTBOUND_REGISTRY_IDS.addListener(this, (reference, packet, ignored3) -> {
            reference.handle(packet);
        });
    }

    /**
     * Initializes the connection with the current server if the connection has been established.
     */
    public void initialize() {
        // Ignore if already initialized
        if (state != HandshakeState.NONE) return;

        // Don't allow if the server doesn't accept any handshakes, don't use our own method as it checks if it's been
        // registered yet which it hasn't! We want to hide the plugin channels from the server since some servers
        // (like Zero Minr) kick on detecting any plugin channels that aren't whitelisted, whereas no client mod will
        // care what a server asks for.
        // Also, it's fine if Noxesium simply doesn't enable unless the server needs it since it's a mod meant for the
        // server to customise the client.
        if (!ClientPlayNetworking.canSend(HandshakePackets.SERVERBOUND_HANDSHAKE.id())) return;

        // Check if the connection has been established first, just in case
        if (Minecraft.getInstance().getConnection() == null) return;

        // Register the handshake packet collection at all times
        HandshakePackets.INSTANCE.register();

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
            var encryptionKeyFile = entrypoint.getEncryptionKey();
            if (encryptionKeyFile != null) {
                try {
                    try (var stream = encryptionKeyFile.openStream()) {
                        byte[] keyBytes = Base64.getDecoder().decode(stream.readAllBytes());
                        var keySpec = new SecretKeySpec(keyBytes, "AES");
                        var iv = new IvParameterSpec(
                                new byte[] {-76, 14, 22, -123, 63, 60, -50, 23, -118, 10, 105, -127, 85, 41, -97, 37});
                        var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
                        var encryptedId =
                                Base64.getEncoder().encodeToString(cipher.doFinal(id.getBytes(Charsets.UTF_8)));
                        var encryptedSecret =
                                Base64.getEncoder().encodeToString(cipher.doFinal(secret.getBytes(Charsets.UTF_8)));
                        ids.put(encryptedId, encryptedSecret);
                        return;
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
        System.out.println("Received " + packet.entrypoints());
        var api = NoxesiumApi.getInstance();
        var fabricApi = NoxesiumNmsApi.getInstance();
        var entrypoints = new ArrayList<EntrypointProtocol>();
        for (var pair : packet.entrypoints().entrySet()) {
            var id = pair.getKey();
            var challengeResult = pair.getValue();

            // Determine which entrypoint this is
            var entrypoint = api.getEntrypoint(id);
            if (entrypoint == null) continue;

            // If the challenge result is invalid, log an error!
            if (!Objects.equals(challenges.get(entrypoint), challengeResult)) {
                NoxesiumApi.getLogger().error("Server responded with invalid decryption for entrypoint id {}", id);
                continue;
            }

            // Initialize this entrypoint and add it to the list
            if (entrypoint instanceof ClientNoxesiumEntrypoint clientEntrypoint) {
                entrypoints.add(new EntrypointProtocol(
                        entrypoint.getId(), clientEntrypoint.getProtocolVersion(), clientEntrypoint.getRawVersion()));
                entrypoint.getAllFeatures().forEach(api::registerFeature);
                entrypoint.getRegistryCollections().forEach(RegistryCollection::register);
                clientEntrypoint.getPacketCollections().forEach(fabricApi::registerPackets);
            }
        }

        // Inform the server about the handshake success
        NoxesiumServerboundNetworking.send(new ServerboundHandshakeAcknowledgePacket(entrypoints));
        NoxesiumApi.getLogger()
                .info(
                        "Successfully authenticated {} out of {} entrypoints with joined server",
                        entrypoints.size(),
                        NoxesiumApi.getInstance().getAllEntrypoints().size());
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
        challenges.clear();
        HandshakePackets.INSTANCE.unregister();
        NoxesiumApi.getInstance().unregisterAll();
        NoxesiumNmsApi.getInstance().unregisterAll();
        Minecraft.getInstance().noxesium$reloadComponents();
    }
}
