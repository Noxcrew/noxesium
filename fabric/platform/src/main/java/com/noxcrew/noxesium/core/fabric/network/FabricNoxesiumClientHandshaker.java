package com.noxcrew.noxesium.core.fabric.network;

import com.noxcrew.noxesium.api.nms.network.HandshakePackets;
import com.noxcrew.noxesium.core.nms.network.NoxesiumClientHandshaker;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

/**
 * Manages initialization of the Noxesium handshake protocol.
 */
public class FabricNoxesiumClientHandshaker extends NoxesiumClientHandshaker {
    /**
     * Registers the initializer.
     */
    public void register() {
        super.register();

        // Every time the client joins a server we send over information on the version being used,
        // we initialize when both packets are known and we are in the PLAY phase, whenever both have
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
    }

    @Override
    public void initialize() {
        // Check if the connection has been established first, just in case.
        // This should not be able to fail, so we just stop trying to authenticate.
        if (Minecraft.getInstance().getConnection() == null) return;

        // Don't allow if the server doesn't accept any handshakes, don't use our own method as it checks if it's been
        // registered yet which it hasn't! We want to hide the plugin channels from the server since some servers
        // (like Zero Minr) kick on detecting any plugin channels that aren't whitelisted, whereas no client mod will
        // care what a server asks for.
        // Also, it's fine if Noxesium simply doesn't enable unless the server needs it since it's a mod meant for the
        // server to customise the client.
        if (!ClientPlayNetworking.canSend(HandshakePackets.SERVERBOUND_HANDSHAKE.id())) return;

        super.initialize();
    }
}
