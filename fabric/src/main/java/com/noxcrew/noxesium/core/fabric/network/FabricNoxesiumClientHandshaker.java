package com.noxcrew.noxesium.core.fabric.network;

import com.noxcrew.noxesium.api.ClientNoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.ConnectionProtocolType;
import com.noxcrew.noxesium.api.network.EntrypointProtocol;
import com.noxcrew.noxesium.api.network.ModInfo;
import com.noxcrew.noxesium.api.network.NoxesiumErrorReason;
import com.noxcrew.noxesium.api.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.api.network.handshake.HandshakePackets;
import com.noxcrew.noxesium.api.network.handshake.NoxesiumClientHandshaker;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
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
        // how the server has its proxy configured.
        C2SPlayChannelEvents.REGISTER.register((ignored1, ignored2, ignored3, channels) -> {
            if (channels.contains(HANDSHAKE_CHANNEL)) {
                initialize();
            }
        });
        C2SPlayChannelEvents.UNREGISTER.register((ignored1, ignored2, ignored3, channels) -> {
            if (channels.contains(HANDSHAKE_CHANNEL)) {
                uninitialize(NoxesiumErrorReason.CHANNEL_UNREGISTERED);
            }
        });

        // Mark down when the protocol changes
        ClientConfigurationConnectionEvents.START.register((ignored1, ignored2) -> {
            NoxesiumServerboundNetworking.getInstance().setConfiguredProtocol(ConnectionProtocolType.CONFIGURATION);
        });
        ClientPlayConnectionEvents.JOIN.register((ignored1, ignored2, ignored3) -> {
            NoxesiumServerboundNetworking.getInstance().setConfiguredProtocol(ConnectionProtocolType.PLAY);

            // Whenever we (re-)enter the PLAY phase we check if we have the handshake channel available
            // and either initialize or break down a previous connection that may have lasted through a config phase.
            if (ClientPlayNetworking.canSend(HANDSHAKE_CHANNEL)) {
                initialize();
            } else {
                uninitialize(NoxesiumErrorReason.CHANNEL_UNREGISTERED);
            }
        });

        // We listen in ConnectionTerminationMixin to disconnecting the server and use that to destroy the connection.

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

        // Don't allow if the server doesn't accept any handshakes, don't use our own method as it checks if
        // it's been registered yet which it hasn't! We want to hide the plugin channels from the server since
        // some servers (like Zero Minr) kick on detecting any plugin channels that aren't whitelisted,
        // whereas no client mod will care what a server asks for.
        // Also, it's fine if Noxesium simply doesn't enable unless the server needs it since it's a mod meant
        // for the server to customise the client.
        if (!ClientPlayNetworking.canSend(HANDSHAKE_CHANNEL)) return;

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

            // Don't include fabric api mods for brevity, do include built-ins so it shares minecraft and java version
            if (Objects.equals(
                    modContainer.getMetadata().getContact().get("homepage").orElse(null), "https://fabricmc.net"))
                return;

            mods.add(new ModInfo(
                    modContainer.getMetadata().getId(),
                    modContainer.getMetadata().getVersion().getFriendlyString()));
        });
        return mods;
    }
}
