package com.noxcrew.noxesium.core.fabric.network;

import com.noxcrew.noxesium.api.ClientNoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.EntrypointProtocol;
import com.noxcrew.noxesium.api.network.ModInfo;
import com.noxcrew.noxesium.api.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.api.network.handshake.HandshakePackets;
import com.noxcrew.noxesium.api.network.handshake.NoxesiumClientHandshaker;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.C2SConfigurationChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

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
        // how the server has its proxy configured.
        C2SConfigurationChannelEvents.REGISTER.register((ignored1, ignored2, ignored3, channels) -> {
            System.out.println("config register " + channels);
            if (channels.contains(HANDSHAKE_CHANNEL)) {
                initialize();
            }
        });
        C2SPlayChannelEvents.REGISTER.register((ignored1, ignored2, ignored3, channels) -> {
            System.out.println("play register " + channels);
            if (channels.contains(HANDSHAKE_CHANNEL)) {
                initialize();
            }
        });
        C2SConfigurationChannelEvents.UNREGISTER.register((ignored1, ignored2, ignored3, channels) -> {
            System.out.println("config unregister " + channels);
            if (channels.contains(HANDSHAKE_CHANNEL)) {
                uninitialize();
            }
        });
        C2SPlayChannelEvents.UNREGISTER.register((ignored1, ignored2, ignored3, channels) -> {
            System.out.println("play unregister " + channels);
            if (channels.contains(HANDSHAKE_CHANNEL)) {
                uninitialize();
            }
        });

        // Start initialization as well if we enter the CONFIG phase whenever the channel is registered.
        ClientConfigurationConnectionEvents.INIT.register((ignored1, ignored2) -> {
            System.out.println("config init");
            initialize();
        });
        ClientConfigurationConnectionEvents.START.register((ignored1, ignored2) -> {
            System.out.println("config start");
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
        var packetId = ResourceLocation.parse(HandshakePackets.SERVERBOUND_HANDSHAKE.id().asString());
        var currentProtocol = NoxesiumServerboundNetworking.getInstance().getConfiguredProtocol();
        switch (currentProtocol) {
            case CONFIGURATION -> {
                // Check if the packet has been registered yet.
                if (!ClientConfigurationNetworking.canSend(packetId)) return;
            }

            case PLAY -> {
                // Check if the connection has been established first, just in case.
                // This should not be able to fail, so we just stop trying to authenticate.
                if (Minecraft.getInstance().getConnection() == null) return;

                // Don't allow if the server doesn't accept any handshakes, don't use our own method as it checks if
                // it's been
                // registered yet which it hasn't! We want to hide the plugin channels from the server since some
                // servers
                // (like Zero Minr) kick on detecting any plugin channels that aren't whitelisted, whereas no client mod
                // will
                // care what a server asks for.
                // Also, it's fine if Noxesium simply doesn't enable unless the server needs it since it's a mod meant
                // for the
                // server to customise the client.
                if (!ClientPlayNetworking.canSend(packetId)) return;
            }
            case NONE -> {
                return;
            }
        }
        super.initialize();
    }

    @Override
    protected Collection<ModInfo> collectMods(List<EntrypointProtocol> entrypoints) {
        // Determine any mods that provide entrypoints that were not authenticated, those should be hidden!
        var entrypointIds = entrypoints.stream().map(EntrypointProtocol::id).toList();
        var modsToShow = new HashSet<String>();
        var modsToHide = new HashSet<String>();
        FabricLoader.getInstance()
            .getEntrypointContainers("noxesium", ClientNoxesiumEntrypoint.class)
            .forEach(entrypointContainer -> {
                var modId = entrypointContainer.getProvider().getMetadata().getId();
                if (entrypointIds.contains(
                    entrypointContainer.getEntrypoint().getId())) {
                    modsToShow.add(modId);
                } else {
                    modsToHide.add(modId);
                }
            });

        // Some mods may have multiple endpoints, show them if they have any valid ones!
        modsToHide.removeAll(modsToShow);

        // Determine the final list of mods to report to the server
        var mods = new HashSet<ModInfo>();
        FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
            // Ignore mods that are marked as hidden!
            if (modsToHide.contains(modContainer.getMetadata().getId())) return;

            mods.add(new ModInfo(
                modContainer.getMetadata().getId(),
                modContainer.getMetadata().getVersion().getFriendlyString()));
        });
        return mods;
    }
}
