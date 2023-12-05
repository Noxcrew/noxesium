package com.noxcrew.noxesium;

import com.noxcrew.noxesium.api.protocol.ClientSettings;
import com.noxcrew.noxesium.compat.ClothConfigIntegration;
import com.noxcrew.noxesium.feature.render.NoxesiumReloadListener;
import com.noxcrew.noxesium.feature.rule.ServerRuleModule;
import com.noxcrew.noxesium.feature.skull.SkullFontModule;
import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundModule;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.serverbound.ServerboundClientInformationPacket;
import com.noxcrew.noxesium.network.serverbound.ServerboundClientSettingsPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * The main file for the client-side implementation of Noxesium.
 */
public class NoxesiumMod implements ClientModInitializer {

    /**
     * The current protocol version of the mod. Servers can use this version to determine which functionality
     * of Noxesium is available on the client. The protocol version will increment every full release, as such
     * ít is recommended to work with >= comparisons.
     */
    public static final int VERSION = 4;

    public static final String BUKKIT_COMPOUND_ID = "PublicBukkitValues";
    public static final String NAMESPACE = "noxesium";
    public static final String IMMOVABLE_TAG = new ResourceLocation(NAMESPACE, "immovable").toString();

    private static NoxesiumMod instance;

    /**
     * All modules known to Noxesium that have been registered.
     */
    private final Map<Class<? extends NoxesiumModule>, NoxesiumModule> modules = new HashMap<>();

    /**
     * The current maximum supported protocol version.
     */
    private int currentMaxProtocol = VERSION;

    /**
     * Returns the known Noxesium instance.
     */
    public static NoxesiumMod getInstance() {
        return instance;
    }

    /**
     * Adds a new module to the list of modules that should have
     * their hooks called. Available for other mods to use.
     */
    public void registerModule(NoxesiumModule module) {
        modules.put(module.getClass(), module);
    }

    /**
     * Returns the module of type [T] if one is registered.
     */
    @Nullable
    public <T extends NoxesiumModule> T getModule(Class<T> clazz) {
        return (T) modules.get(clazz);
    }

    /**
     * Returns the latest protocol version that is currently supported.
     */
    public int getMaxProtocolVersion() {
        return currentMaxProtocol;
    }

    /**
     * Stores the maximum protocol version of the current server.
     */
    public void setServerVersion(int maxProtocolVersion) {
        currentMaxProtocol = maxProtocolVersion;
    }

    @Override
    public void onInitializeClient() {
        // Store the instance and register all modules
        instance = this;
        registerModule(new ServerRuleModule());
        registerModule(new SkullFontModule());
        registerModule(new NoxesiumSoundModule());

        // Register the config
        if (CompatibilityReferences.isUsingClothConfig()) {
            ClothConfigIntegration.register();
        }

        // Every time the client joins a server we send over information on the version being used
        C2SPlayChannelEvents.REGISTER.register((ignored1, ignored2, ignored3, channels) -> {
            // Find the packet that includes the server asking for the information packet
            if (!channels.contains(NoxesiumPackets.SERVER_CLIENT_INFO.getId())) return;

            // Check if the connection has been established first, just in case
            if (Minecraft.getInstance().getConnection() != null) {
                // Send a packet containing information about the client version of Noxesium
                new ServerboundClientInformationPacket(VERSION).send();

                // Inform the player about the GUI scale of the client
                syncGuiScale();

                // Call connection hooks
                modules.values().forEach(NoxesiumModule::onJoinServer);
            }
        });

        // Call disconnection hooks
        ClientPlayConnectionEvents.DISCONNECT.register((ignored1, ignored2) -> {
            // Reset the current max protocol version
            currentMaxProtocol = VERSION;

            modules.values().forEach(NoxesiumModule::onQuitServer);
        });

        // Register all universal messaging channels
        NoxesiumPackets.registerPackets("universal");

        // Call initialisation on all modules
        modules.values().forEach(NoxesiumModule::onStartup);

        // Register the resource listener
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new NoxesiumReloadListener());
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
