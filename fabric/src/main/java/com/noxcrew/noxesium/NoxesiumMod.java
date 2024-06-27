package com.noxcrew.noxesium;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.protocol.ClientSettings;
import com.noxcrew.noxesium.api.protocol.ProtocolVersion;
import com.noxcrew.noxesium.config.NoxesiumConfig;
import com.noxcrew.noxesium.feature.TeamGlowHotkeys;
import com.noxcrew.noxesium.feature.entity.ExtraEntityDataModule;
import com.noxcrew.noxesium.feature.entity.QibBehaviorModule;
import com.noxcrew.noxesium.feature.model.CustomServerCreativeItems;
import com.noxcrew.noxesium.feature.rule.ServerRuleModule;
import com.noxcrew.noxesium.feature.skull.SkullFontModule;
import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundModule;
import com.noxcrew.noxesium.feature.ui.NoxesiumReloadListener;
import com.noxcrew.noxesium.network.NoxesiumPacketHandling;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.serverbound.ServerboundClientInformationPacket;
import com.noxcrew.noxesium.network.serverbound.ServerboundClientSettingsPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The main file for the client-side implementation of Noxesium.
 */
public class NoxesiumMod implements ClientModInitializer {

    public static final String BUKKIT_COMPOUND_ID = "PublicBukkitValues";
    public static final String IMMOVABLE_TAG = ResourceLocation.fromNamespaceAndPath(ProtocolVersion.NAMESPACE, "immovable").toString();
    public static final String RAW_MODEL_TAG = ResourceLocation.fromNamespaceAndPath(ProtocolVersion.NAMESPACE, "raw_model").toString();

    private static NoxesiumMod instance;

    /**
     * All modules known to Noxesium that have been registered.
     */
    private final Map<Class<? extends NoxesiumModule>, NoxesiumModule> modules = new HashMap<>();

    /**
     * The current maximum supported protocol version.
     */
    private int currentMaxProtocol = ProtocolVersion.VERSION;

    /**
     * Whether the server connection has been initialized correctly.
     */
    private boolean initialized = false;

    private final NoxesiumConfig config = NoxesiumConfig.load();
    private final Logger logger = LoggerFactory.getLogger("Noxesium");
    
    /**
     * Returns the known Noxesium instance.
     */
    public static NoxesiumMod getInstance() {
        return instance;
    }

    /**
     * Returns the logger instance to use.
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Returns the configuration used by Noxesium.
     */
    public NoxesiumConfig getConfig() {
        return config;
    }

    /**
     * Adds a new module to the list of modules that should have
     * their hooks called. Available for other mods to use.
     */
    public void registerModule(NoxesiumModule module) {
        modules.put(module.getClass(), module);
        module.onStartup();
    }

    /**
     * Returns the module of type [T] if one is registered.
     */
    @NotNull
    public <T extends NoxesiumModule> T getModule(Class<T> clazz) {
        return (T) Preconditions.checkNotNull(modules.get(clazz), "Could not get module " + clazz.getSimpleName());
    }

    /**
     * Returns all registered Noxesium modules.
     */
    public Collection<NoxesiumModule> getAllModules() {
        return modules.values();
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
        // Don't initialize twice! This allows other mods to call NoxesiumMod#onInitializeClient to ensure it's done initializing.
        if (instance == this) return;

        // Store the instance and register all modules
        instance = this;

        registerModule(new ServerRuleModule());
        registerModule(new SkullFontModule());
        registerModule(new NoxesiumSoundModule());
        registerModule(new TeamGlowHotkeys());
        registerModule(new NoxesiumPacketHandling());
        registerModule(new CustomServerCreativeItems());
        registerModule(new ExtraEntityDataModule());
        registerModule(new QibBehaviorModule());

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
            // Reset the current max protocol version
            currentMaxProtocol = ProtocolVersion.VERSION;
            initialized = false;

            // Handle quitting the server
            modules.values().forEach(NoxesiumModule::onQuitServer);

            // Unregister additional packets
            NoxesiumPackets.unregisterPackets();
        });

        // Register all universal messaging channels
        NoxesiumPackets.registerPackets("universal");

        // Register the resource listener
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new NoxesiumReloadListener());
    }

    /**
     * Initializes the connection with the current server if the connection has been established.
     */
    private void initialize() {
        // Ignore if already initialized
        if (initialized) return;

        // Don't allow if the server doesn't support Noxesium
        if (!ClientPlayNetworking.canSend(NoxesiumPackets.CLIENT_INFO.id())) return;

        // Check if the connection has been established first, just in case
        if (Minecraft.getInstance().getConnection() != null) {
            // Mark down that we are initializing the connection
            initialized = true;

            // Send a packet containing information about the client version of Noxesium
            new ServerboundClientInformationPacket(ProtocolVersion.VERSION, FabricLoader.getInstance().getModContainer(ProtocolVersion.NAMESPACE).map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("unknown")).send();

            // Inform the player about the GUI scale of the client
            syncGuiScale();

            // Call connection hooks
            modules.values().forEach(NoxesiumModule::onJoinServer);
        }
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
