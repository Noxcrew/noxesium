package com.noxcrew.noxesium.api.fabric;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.fabric.feature.NoxesiumFeature;
import com.noxcrew.noxesium.api.fabric.network.PacketCollection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides shared API for Noxesium's various available APIs.
 */
public class NoxesiumApi {
    private static NoxesiumApi instance;
    private static final Logger logger = LoggerFactory.getLogger("Noxesium");

    private final Map<Class<? extends NoxesiumFeature>, NoxesiumFeature> features = new HashMap<>();
    private final Set<PacketCollection> packets = new HashSet<>();
    private final Map<String, NoxesiumEntrypoint> entrypoints = new HashMap<>();

    /**
     * Returns the singleton instance of the Noxesium API.
     */
    public static NoxesiumApi getInstance() {
        if (instance == null) {
            instance = new NoxesiumApi();
        }
        return instance;
    }

    /**
     * Returns the logger instance to use.
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Registers a new entrypoint.
     */
    public void registerEndpoint(NoxesiumEntrypoint entrypoint) {
        entrypoints.put(entrypoint.getId(), entrypoint);
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
        feature.register();
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
        features.values().forEach(NoxesiumFeature::unregister);
        features.clear();
        packets.forEach(PacketCollection::unregister);
        packets.clear();
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
     * Returns all registered entry points.
     */
    public Collection<NoxesiumEntrypoint> getAllEntrypoints() {
        return entrypoints.values();
    }
}
