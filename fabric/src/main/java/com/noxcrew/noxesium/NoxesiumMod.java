package com.noxcrew.noxesium;

import com.noxcrew.noxesium.api.protocol.ClientSettings;
import com.noxcrew.noxesium.feature.rule.ServerRuleModule;
import com.noxcrew.noxesium.feature.skull.SkullFontModule;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.serverbound.ServerboundClientInformationPacket;
import com.noxcrew.noxesium.network.serverbound.ServerboundClientSettingsPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

/**
 * The main file for the client-side implementation of Noxesium.
 */
public class NoxesiumMod implements ClientModInitializer {

    /**
     * The current protocol version of the mod. Servers can use this version to determine which functionality
     * of Noxesium is available on the client. The protocol version will increment every full release, as such
     * ít is recommended to work with >= comparisons.
     */
    public static final int VERSION = 3;

    public static final String BUKKIT_COMPOUND_ID = "PublicBukkitValues";
    public static final String NAMESPACE = "noxesium";
    public static final String IMMOVABLE_TAG = new ResourceLocation(NAMESPACE, "immovable").toString();

    /**
     * All modules known to Noxesium that need to be registered.
     */
    private static final Set<NoxesiumModule> modules = new HashSet<>(Set.of(
            ServerRuleModule.getInstance(),
            SkullFontModule.getInstance()
    ));

    /**
     * The current maximum supported protocol version.
     */
    private static int currentMaxProtocol = VERSION;

    /**
     * Adds a new module to the list of modules that should have
     * their hooks called. Available for other mods to use.
     */
    public static void registerModule(NoxesiumModule module) {
        modules.add(module);
    }

    /**
     * Returns the latest protocol version that is currently supported.
     */
    public static int getMaxProtocolVersion() {
        return currentMaxProtocol;
    }

    /**
     * Stores the maximum protocol version of the current server.
     */
    public static void setServerVersion(int maxProtocolVersion) {
        currentMaxProtocol = maxProtocolVersion;
    }

    @Override
    public void onInitializeClient() {
        // Every time the client joins a server we send over information on the version being used
        ClientPlayConnectionEvents.JOIN.register((ignored1, ignored2, ignored3) -> {
            // Send a packet containing information about the client version of Noxesium
            if (Minecraft.getInstance().getConnection() != null) {
                new ServerboundClientInformationPacket(VERSION).send();

                // Inform the player about the GUI scale of the client
                syncGuiScale();

                // Call connection hooks
                modules.forEach(NoxesiumModule::onJoinServer);
            }
        });

        // Call disconnection hooks
        ClientPlayConnectionEvents.DISCONNECT.register((ignored1, ignored2) -> {
            modules.forEach(NoxesiumModule::onQuitServer);

            // Reset the current max protocol version
            currentMaxProtocol = VERSION;
        });

        // Register all universal messaging channels
        NoxesiumPackets.registerPackets("universal");

        // Call initialisation on all modules
        modules.forEach(NoxesiumModule::onStartup);
    }

    /**
     * Sends a packet to the server containing the GUI scale of the client which
     * allows servers to more accurately adapt their UI to clients.
     */
    public static void syncGuiScale() {
        // Don't send if there is no established connection
        if (Minecraft.getInstance().getConnection() == null) return;

        var window = Minecraft.getInstance().getWindow();
        var options = Minecraft.getInstance().options;

        new ServerboundClientSettingsPacket(
                new ClientSettings(
                        options.guiScale().get(),
                        window.getGuiScale(),
                        window.getGuiScaledWidth(),
                        window.getGuiScaledHeight(),
                        Minecraft.getInstance().isEnforceUnicode(),
                        options.touchscreen().get(),
                        options.notificationDisplayTime().get()
                )
        ).send();
    }
}
