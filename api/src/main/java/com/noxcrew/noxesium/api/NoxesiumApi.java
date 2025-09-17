package com.noxcrew.noxesium.api;

import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.api.network.EntrypointProtocol;
import com.noxcrew.noxesium.api.network.PacketCollection;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Provides shared API for Noxesium's various available APIs.
 */
public class NoxesiumApi {
    private static final NoxesiumApi INSTANCE = new NoxesiumApi();
    private static final Logger logger = LoggerFactory.getLogger("Noxesium");

    private final Map<Class<? extends NoxesiumFeature>, NoxesiumFeature> features = new HashMap<>();
    private final Map<String, NoxesiumEntrypoint> entrypoints = new HashMap<>();
    private final Set<PacketCollection> packets = new HashSet<>();
    private final List<NoxesiumEntrypoint> activeEntrypoints = new ArrayList<>();
    private NoxesiumSide side = NoxesiumSide.SERVER;

    /**
     * Returns the singleton instance of the Noxesium API.
     */
    public static NoxesiumApi getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the logger instance to use.
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Returns which side Noxesium is running on.
     */
    public NoxesiumSide getSide() {
        return side;
    }

    /**
     * Sets which side Noxesium is running on.
     */
    public void setSide(NoxesiumSide side) {
        this.side = side;
    }

    /**
     * Activates the entrypoint with the given protocol.
     */
    public void activateEntrypoint(EntrypointProtocol protocol) {
        var entrypoint = entrypoints.get(protocol.id());
        if (entrypoint == null) return;
        entrypoint.getRegistryCollections().forEach(it -> it.register(entrypoint));
        entrypoint.getPacketCollections().forEach(it -> registerPackets(entrypoint, it));
        activeEntrypoints.add(entrypoint);
    }

    /**
     * Registers a new entrypoint.
     */
    public void registerEntrypoint(NoxesiumEntrypoint entrypoint) {
        entrypoints.put(entrypoint.getId(), entrypoint);
    }

    /**
     * Registers a new entrypoint and instantly activates it. Meant for the server-side
     * where entrypoints are immediately active.
     */
    public void registerAndActivateEntrypoint(NoxesiumEntrypoint entrypoint) {
        registerEntrypoint(entrypoint);
        entrypoint.getRegistryCollections().forEach(it -> it.register(entrypoint));
        entrypoint.getPacketCollections().forEach(it -> registerPackets(entrypoint, it));
        entrypoint.getAllFeatures().forEach(this::registerFeature);
        activeEntrypoints.add(entrypoint);
    }

    /**
     * Adds a new feature to the list of features that should
     * be registered. Usually called by the initializer.
     */
    public void registerFeature(NoxesiumFeature feature) {
        if (!features.containsKey(feature.getClass())) {
            features.put(feature.getClass(), feature);
            feature.register();
        }
    }

    /**
     * Registers a new collection of packets.
     * Usually called by the initializer.
     */
    public void registerPackets(NoxesiumEntrypoint entrypoint, PacketCollection collection) {
        packets.add(collection);
        collection.register(entrypoint);
    }

    /**
     * Unregisters all features collections.
     */
    public void unregisterAll() {
        activeEntrypoints.clear();
        features.values().forEach(NoxesiumFeature::unregister);
        features.clear();
        packets.forEach(PacketCollection::unregister);
        packets.clear();
        NoxesiumRegistries.REGISTRIES.forEach(NoxesiumRegistry::reset);
    }

    /**
     * Returns the feature of type [T] if one is registered, or `null` otherwise.
     */
    @Nullable
    public <T extends NoxesiumFeature> T getFeatureOrNull(Class<T> clazz) {
        return (T) features.get(clazz);
    }

    /**
     * Returns the feature of type [T], wrapped in an optional.
     */
    @NotNull
    public <T extends NoxesiumFeature> Optional<T> getFeatureOptional(Class<T> clazz) {
        return Optional.ofNullable(getFeatureOrNull(clazz));
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
     * Returns all active entrypoints.
     */
    public Collection<NoxesiumEntrypoint> getActiveEntrypoints() {
        return activeEntrypoints;
    }

    /**
     * Returns all registered entry points.
     */
    public Collection<NoxesiumEntrypoint> getAllEntrypoints() {
        return entrypoints.values();
    }
}
