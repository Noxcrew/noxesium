package com.noxcrew.noxesium.api.fabric.registry;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.fabric.component.NoxesiumComponentListener;
import com.noxcrew.noxesium.api.fabric.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry;
import java.util.LinkedHashMap;
import java.util.Map;
import net.kyori.adventure.key.Key;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * A collection of registry entries that can be (re-)registered as a group.
 */
public final class RegistryCollection<T> {
    private final NoxesiumRegistry<T> registry;
    private final Map<Key, T> entries = new LinkedHashMap<>();

    public RegistryCollection(NoxesiumRegistry<T> registry) {
        this.registry = registry;
    }

    /**
     * Registers a new component type to the registry.
     */
    public static <C> NoxesiumComponentType<C> register(
            RegistryCollection<NoxesiumComponentType<?>> collection,
            String namespace,
            String key,
            Codec<C> codec,
            StreamCodec<? super RegistryFriendlyByteBuf, C> streamCodec,
            NoxesiumComponentListener<C, ?> listener) {
        var component = new NoxesiumComponentType<>(namespace, key, codec, streamCodec, listener);
        collection.register(Key.key(namespace, key), component);
        return component;
    }

    /**
     * Adds a new registration.
     */
    public T register(Key key, T value) {
        entries.put(key, value);
        return value;
    }

    /**
     * Registers all registry entries.
     */
    public void register() {
        for (var entry : entries.entrySet()) {
            registry.register(entry.getKey(), entry.getValue());
        }
    }
}
