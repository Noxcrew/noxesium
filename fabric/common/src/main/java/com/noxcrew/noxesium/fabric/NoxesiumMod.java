package com.noxcrew.noxesium.fabric;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.fabric.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.fabric.feature.NoxesiumFeature;
import com.noxcrew.noxesium.api.fabric.network.NoxesiumNetworking;
import com.noxcrew.noxesium.api.fabric.network.PacketCollection;
import com.noxcrew.noxesium.fabric.config.NoxesiumConfig;
import com.noxcrew.noxesium.fabric.feature.entity.ExtraEntityData;
import com.noxcrew.noxesium.fabric.feature.entity.SpatialInteractionEntityTree;
import com.noxcrew.noxesium.fabric.feature.render.CustomRenderTypes;
import com.noxcrew.noxesium.fabric.feature.rule.ServerRules;
import com.noxcrew.noxesium.fabric.network.handshake.NoxesiumInitializer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base for the fabric mod which loads in all entrypoints and manages registries.
 */
public class NoxesiumMod implements ClientModInitializer {
    private static NoxesiumMod instance;

    private final Logger logger = LoggerFactory.getLogger("Noxesium");
    private final Map<Class<? extends NoxesiumFeature>, NoxesiumFeature> features = new HashMap<>();
    private final Set<PacketCollection> packets = new HashSet<>();
    private final Map<String, NoxesiumEntrypoint> entrypoints = new HashMap<>();
    private final NoxesiumConfig config;

    /**
     * Returns the known Noxesium instance.
     */
    public static NoxesiumMod getInstance() {
        return instance;
    }

    /**
     * Creates a new NoxesiumMod instance.
     */
    public NoxesiumMod() {
        instance = this;
        config = NoxesiumConfig.load();

        // Set the packet dumping values which are needed by the API
        NoxesiumNetworking.dumpIncomingPackets = config.getDumpIncomingPackets();
        NoxesiumNetworking.dumpOutgoingPackets = config.getDumpOutgoingPackets();
    }

    @Override
    public void onInitializeClient() {
        // Go through all entrypoints and register them
        FabricLoader.getInstance()
                .getEntrypointContainers("noxesium", NoxesiumEntrypoint.class)
                .forEach(entrypoint -> {
                    try {
                        entrypoints.put(entrypoint.getEntrypoint().getId(), entrypoint.getEntrypoint());
                    } catch (Exception e) {
                        getLogger()
                                .error(
                                        "Failed to initialize Noxesium entrypoint from mod {}",
                                        entrypoint.getProvider().getMetadata().getId());
                    }
                });

        // Log how many entrypoints were successfully loaded
        getLogger().info("Loaded {} extensions to Noxesium", entrypoints.size());

        // Set up the initializer
        new NoxesiumInitializer().register();

        // Trigger registration of all server and entity rules and render types
        Object ignored = ServerRules.DISABLE_SPIN_ATTACK_COLLISIONS;
        ignored = ExtraEntityData.DISABLE_BUBBLES;
        ignored = CustomRenderTypes.linesNoDepth();

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
     * Adds a new feature to the list of features that should
     * be registered. Usually called by the initializer.
     */
    public void registerFeature(NoxesiumFeature feature) {
        Preconditions.checkState(
                !features.containsKey(feature.getClass()),
                "Feature " + feature.getClass().getSimpleName() + " is already registered");
        features.put(feature.getClass(), feature);
        feature.onRegister();
    }

    /**
     * Registers a new collection of packets.
     * Usually called by the initializer.
     */
    public void registerPackets(PacketCollection collection) {
        packets.add(collection);
        collection.register();
    }

    /**
     * Unregisters all features and packet collections.
     */
    public void unregisterAll() {
        features.values().forEach(NoxesiumFeature::onUnregister);
        features.clear();
        packets.forEach(PacketCollection::unregister);
        packets.clear();
    }

    /**
     * Returns the feature of type [T] if one is registered.
     */
    @NotNull
    public <T extends NoxesiumFeature> T getFeature(Class<T> clazz) {
        return (T) Preconditions.checkNotNull(features.get(clazz), "Could not get feature " + clazz.getSimpleName());
    }

    /**
     * Returns all registered features.
     */
    public Collection<NoxesiumFeature> getAllFeatures() {
        return features.values();
    }

    /**
     * Returns the entrypoint with the given id.
     */
    @Nullable
    public NoxesiumEntrypoint getEntrypoint(String id) {
        return entrypoints.get(id);
    }

    /**
     * Returns all registered entry points.
     */
    public Collection<NoxesiumEntrypoint> getAllEntrypoints() {
        return entrypoints.values();
    }
}
