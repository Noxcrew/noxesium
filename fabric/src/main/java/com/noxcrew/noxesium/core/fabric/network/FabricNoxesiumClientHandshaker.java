package com.noxcrew.noxesium.core.fabric.network;

import com.noxcrew.noxesium.api.network.handshake.HandshakePackets;
import com.noxcrew.noxesium.api.network.handshake.NoxesiumClientHandshaker;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

/**
 * Manages initialization of the Noxesium handshake protocol.
 */
public class FabricNoxesiumClientHandshaker extends NoxesiumClientHandshaker {
    /**
     * The channel used by the handshake packet.
     */
    private static final ResourceLocation HANDSHAKE_CHANNEL =
            ResourceLocation.parse(HandshakePackets.SERVERBOUND_HANDSHAKE.id().asString());

    /**
     * Registers the initializer.
     */
    public void register() {
        super.register();

        // We initialize and uninitialize the handshake whenever the handshake channel is registered or unregistered by
        // the server, we use this to detect when it starts and stops being available. This should work regardless of
        // how
        // the server has its proxy configured.
        C2SPlayChannelEvents.REGISTER.register((ignored1, ignored2, ignored3, channels) -> {
            if (channels.contains(HANDSHAKE_CHANNEL)) {
                initialize();
            }
        });
        C2SPlayChannelEvents.UNREGISTER.register((ignored1, ignored2, ignored3, channels) -> {
            if (channels.contains(HANDSHAKE_CHANNEL)) {
                uninitialize();
            }
        });

        // Start initialization as well if we enter the PLAY phase whenever the channel is registered.
        ClientPlayConnectionEvents.JOIN.register((ignored1, ignored2, ignored3) -> {
            initialize();
        });

        // We listen in ConnectionTerminationMixin to any instance of leaving the PLAY phase and uninitialize from
        // there.

        // Attempt to re-run handshaking 10s after a previous handshake went wrong, this waits
        // out any possible server switches that may have occurred mid-handshake.
        ClientTickEvents.END_CLIENT_TICK.register((ignored1) -> {
            tick();
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
        if (!ClientPlayNetworking.canSend(ResourceLocation.parse(
                HandshakePackets.SERVERBOUND_HANDSHAKE.id().asString()))) return;

        super.initialize();
    }
}
