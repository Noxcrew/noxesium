package com.noxcrew.noxesium;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.shaders.CompiledShader;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.protocol.ClientSettings;
import com.noxcrew.noxesium.config.NoxesiumConfig;
import com.noxcrew.noxesium.feature.CustomCoreShaders;
import com.noxcrew.noxesium.feature.CustomRenderTypes;
import com.noxcrew.noxesium.feature.TeamGlowHotkeys;
import com.noxcrew.noxesium.feature.entity.ExtraEntityData;
import com.noxcrew.noxesium.feature.entity.ExtraEntityDataModule;
import com.noxcrew.noxesium.feature.entity.QibBehaviorModule;
import com.noxcrew.noxesium.feature.entity.SpatialDebuggingModule;
import com.noxcrew.noxesium.feature.entity.SpatialInteractionEntityTree;
import com.noxcrew.noxesium.feature.rule.ServerRuleModule;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import com.noxcrew.noxesium.feature.skull.SkullFontModule;
import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundModule;
import com.noxcrew.noxesium.feature.ui.layer.LayeredDrawExtension;
import com.noxcrew.noxesium.feature.ui.render.api.NoxesiumRenderStateHolder;
import com.noxcrew.noxesium.feature.ui.render.screen.ScreenRenderingHolder;
import com.noxcrew.noxesium.mixin.ui.ext.GuiExt;
import com.noxcrew.noxesium.network.NoxesiumPacketHandling;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.serverbound.ServerboundClientInformationPacket;
import com.noxcrew.noxesium.network.serverbound.ServerboundClientSettingsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * The main file for the client-side implementation of Noxesium.
 */
public class NoxesiumMod {

    private static NoxesiumMod instance;
    private static NoxesiumPlatformHook platform;

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
     * The mapping of cached shaders.
     */
    @Nullable
    private Map<ResourceLocation, Resource> cachedShaders = null;

    private final NoxesiumConfig config;
    private final Logger logger = LoggerFactory.getLogger("Noxesium");

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

        // Register all universal messaging channels
        NoxesiumPackets.registerPackets("universal");

        // Register all default modules
        instance.registerModule(new ServerRuleModule());
        instance.registerModule(new SkullFontModule());
        instance.registerModule(new NoxesiumSoundModule());
        instance.registerModule(new TeamGlowHotkeys());
        instance.registerModule(new NoxesiumPacketHandling());
        instance.registerModule(new ExtraEntityDataModule());
        instance.registerModule(new QibBehaviorModule());
        instance.registerModule(new SpatialDebuggingModule());

        // Trigger registration of all server and entity rules and shaders
        Object ignored = ServerRules.DISABLE_SPIN_ATTACK_COLLISIONS;
        ignored = ExtraEntityData.DISABLE_BUBBLES;
        ignored = CustomCoreShaders.BLIT_SCREEN_MULTIPLE;
        ignored = CustomRenderTypes.linesNoDepth();

        // Run rebuilds on a separate thread to not destroy fps unnecessarily
        var rebuildThread = new Thread("Noxesium Spatial Container Rebuild Thread") {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(2500);
                        SpatialInteractionEntityTree.rebuild();
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
            }
        };
        rebuildThread.setDaemon(true);
        rebuildThread.start();

        // Also run frame comparisons on another thread
        var frameComparisonThread = new Thread("Noxesium Frame Comparison Thread") {
            @Override
            public void run() {
                while (true) {
                    try {
                        forEachRenderStateHolder((it) -> {
                            var state = it.get();
                            if (state != null) {
                                state.tick();
                            }
                        });
                        Thread.sleep(20);
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
            }
        };
        frameComparisonThread.setDaemon(true);
        frameComparisonThread.start();
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
     * Returns a cache with all shaders in the Sodium namespace.
     */
    @Nullable
    public Map<ResourceLocation, Resource> getCachedShaders() {
        return cachedShaders;
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
            new ServerboundClientInformationPacket(NoxesiumReferences.VERSION, getPlatform().getNoxesiumVersion()).send();

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

    /**
     * Runs [consumer] for each render state holder.
     */
    public static void forEachRenderStateHolder(Consumer<NoxesiumRenderStateHolder<?>> consumer) {
        var gui = ((GuiExt) Minecraft.getInstance().gui);
        if (gui != null) {
            var layeredDraw = ((LayeredDrawExtension) gui.getLayers()).noxesium$get();
            if (layeredDraw != null) {
                consumer.accept(layeredDraw);
            }
        }
        consumer.accept(ScreenRenderingHolder.getInstance());
    }

    /**
     * Caches all shaders that have been loaded. Used by Sodium core shader hooks.
     */
    public static CompletableFuture<Void> cacheShaders(ResourceManager manager) {
        return CompletableFuture.supplyAsync(() -> {
            var map = manager.listResources(
                    "shaders",
                    folder -> {
                        // We include all namespaces because you need to be able to import shaders from elsewhere!
                        var s = folder.getPath();
                        return s.endsWith(".json")
                                || CompiledShader.Type.byLocation(folder) != null
                                || s.endsWith(".glsl");
                    }
            );
            var cache = new HashMap<ResourceLocation, Resource>();
            map.forEach((key, value) -> {
                try (InputStream inputstream = value.open()) {
                    byte[] abyte = inputstream.readAllBytes();
                    cache.put(ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getPath().substring("shaders/".length())), new Resource(value.source(), () -> new ByteArrayInputStream(abyte)));
                } catch (Exception exception) {
                    NoxesiumMod.getInstance().getLogger().warn("Failed to read resource {}", key, exception);
                }
            });

            // Save the shaders here instead of in apply so we go before any other resource re-loader!
            instance.cachedShaders = cache;
            return null;
        });
    }
}
