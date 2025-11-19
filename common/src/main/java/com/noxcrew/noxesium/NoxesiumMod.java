package com.noxcrew.noxesium;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.protocol.ClientSettings;
import com.noxcrew.noxesium.config.NoxesiumConfig;
import com.noxcrew.noxesium.feature.CustomRenderTypes;
import com.noxcrew.noxesium.feature.TeamGlowHotkeys;
import com.noxcrew.noxesium.feature.entity.ExtraEntityData;
import com.noxcrew.noxesium.feature.entity.ExtraEntityDataModule;
import com.noxcrew.noxesium.feature.entity.QibBehaviorModule;
import com.noxcrew.noxesium.feature.entity.SpatialDebuggingRenderer;
import com.noxcrew.noxesium.feature.entity.SpatialInteractionEntityTree;
import com.noxcrew.noxesium.feature.rule.ServerRuleModule;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundModule;
import com.noxcrew.noxesium.network.NoxesiumPacketHandling;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.serverbound.ServerboundClientInformationPacket;
import com.noxcrew.noxesium.network.serverbound.ServerboundClientSettingsPacket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main file for the client-side implementation of Noxesium.
 */
public class NoxesiumMod {

    private static NoxesiumMod instance;
    private static NoxesiumPlatformHook platform;

    private final NoxesiumConfig config;
    private final Logger logger = LoggerFactory.getLogger("Noxesium");
    private final Queue<Runnable> mainThreadTasks = new ConcurrentLinkedQueue();

    /**
     * All modules known to Noxesium that have been registered.
     */
    private final Map<Class<? extends NoxesiumModule>, NoxesiumModule> modules = new HashMap<>();

    /**
     * The current maximum supported protocol version.
     */
    private int currentMaxProtocol = NoxesiumReferences.VERSION;

    /**
     * Whether the server connection has been initialized correctly.
     */
    private boolean initialized = false;

    /**
     * Returns the known Noxesium instance.
     */
    public static NoxesiumMod getInstance() {
        return instance;
    }

    /**
     * Returns the Noxesium platform hook.
     */
    public static NoxesiumPlatformHook getPlatform() {
        return platform;
    }

    /**
     * Creates a new NoxesiumMod instance.
     */
    public NoxesiumMod(NoxesiumPlatformHook platformHook) {
        instance = this;
        platform = platformHook;
        config = NoxesiumConfig.load();

        // Register a main thread hook
        platformHook.registerTickEventHandler(this::runMainThreadTasks);

        // Register all universal messaging channels
        NoxesiumPackets.registerPackets("universal");

        // Register all default modules
        instance.registerModule(new ServerRuleModule());
        instance.registerModule(new NoxesiumSoundModule());
        if (NoxesiumMod.getInstance().getConfig().showGlowingSettings) {
            instance.registerModule(new TeamGlowHotkeys());
        }
        instance.registerModule(new NoxesiumPacketHandling());
        instance.registerModule(new ExtraEntityDataModule());
        instance.registerModule(new QibBehaviorModule());
        instance.registerModule(new SpatialDebuggingRenderer());

        // Trigger registration of all server and entity rules and shaders
        Object ignored = ServerRules.DISABLE_SPIN_ATTACK_COLLISIONS;
        ignored = ExtraEntityData.DISABLE_BUBBLES;
        ignored = CustomRenderTypes.textBackgroundSeeThroughWithDepth();

        // Run rebuilds on a separate thread to not destroy fps unnecessarily.
        var backgroundTaskThread = new Thread("Noxesium Background Task Thread") {
            @Override
            public void run() {
                while (true) {
                    try {
                        SpatialInteractionEntityTree.rebuild();
                        Thread.sleep(20);
                    } catch (InterruptedException ex) {
                        return;
                    } catch (Exception ex) {
                        logger.error("Caught exception from Noxesium Background Task Thread", ex);
                    }
                }
            }
        };
        backgroundTaskThread.setDaemon(true);
        backgroundTaskThread.start();
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
     * Runs runnable on the main thread.
     */
    public void ensureMain(Runnable runnable) {
        mainThreadTasks.add(runnable);
    }

    /**
     * Runs pending main thread tasks.
     */
    public void runMainThreadTasks() {
        Runnable task;
        while ((task = mainThreadTasks.poll()) != null) {
            task.run();
        }
    }

    /**
     * Adds a new module to the list of modules that should have
     * their hooks called. Available for other mods to use.
     */
    public void registerModule(NoxesiumModule module) {
        modules.put(module.getClass(), module);
        module.onStartup();

        // Run onGroupRegistered for registered groups
        for (var group : NoxesiumPackets.getRegisteredGroups()) {
            module.onGroupRegistered(group);
        }
    }

    /**
     * Returns the module of type [T] if one is registered as an optional.
     */
    public <T extends NoxesiumModule> Optional<T> getOptionalModule(Class<T> clazz) {
        return Optional.ofNullable((T) modules.get(clazz));
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

    /**
     * Initializes the connection with the current server if the connection has been established.
     */
    public boolean initialize() {
        // Ignore if already initialized
        if (initialized) return false;

        // Don't allow if the server doesn't support Noxesium
        if (!NoxesiumPackets.canSend(NoxesiumPackets.SERVER_CLIENT_INFO)) return false;

        // Check if the connection has been established first, just in case
        if (Minecraft.getInstance().getConnection() != null) {
            // Mark down that we are initializing the connection
            initialized = true;

            // Send a packet containing information about the client version of Noxesium
            new ServerboundClientInformationPacket(
                            NoxesiumReferences.VERSION, getPlatform().getNoxesiumVersion())
                    .send();

            // Inform the player about the GUI scale of the client
            syncGuiScale();

            // Call connection hooks
            modules.values().forEach(NoxesiumModule::onJoinServer);
            return true;
        }
        return false;
    }

    /**
     * Un-initializes the connection with the server.
     */
    public boolean uninitialize() {
        if (!initialized) return false;

        // Reset the current max protocol version
        currentMaxProtocol = NoxesiumReferences.VERSION;
        initialized = false;

        // Handle quitting the server
        modules.values().forEach(NoxesiumModule::onQuitServer);

        // Unregister additional packets
        NoxesiumPackets.unregisterPackets();
        return true;
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

        new ServerboundClientSettingsPacket(new ClientSettings(
                        options.guiScale().get(),
                        window.getGuiScale(),
                        window.getGuiScaledWidth(),
                        window.getGuiScaledHeight(),
                        Minecraft.getInstance().isEnforceUnicode(),
                        options.touchscreen().get(),
                        options.notificationDisplayTime().get()))
                .send();
    }
}
