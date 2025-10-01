package com.noxcrew.noxesium.api.component;

import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

/**
 * Provides a basic implementation of a mutable component holder.
 */
public class SimpleMutableNoxesiumComponentHolder implements MutableNoxesiumComponentHolder {
    protected final Map<NoxesiumComponentType<?>, Object> values = new HashMap<>();
    protected final Set<NoxesiumComponentType<?>> modified = new HashSet<>();
    protected final NoxesiumRegistry<NoxesiumComponentType<?>> registry;

    public SimpleMutableNoxesiumComponentHolder(final NoxesiumRegistry<NoxesiumComponentType<?>> registry) {
        this.registry = registry;
    }

    @Override
    public <T> @Nullable T noxesium$getComponent(NoxesiumComponentType<T> component) {
        return (T) values.get(component);
    }

    @Override
    public boolean noxesium$hasComponent(NoxesiumComponentType<?> component) {
        return values.containsKey(component);
    }

    @Override
    public <T> void noxesium$setComponent(NoxesiumComponentType<T> component, @Nullable T value) {
        var oldValue = values.get(component);
        if (value == null) {
            values.remove(component);
        } else {
            values.put(component, value);
        }
        if (!Objects.equals(oldValue, value)) {
            modified.add(component);
        }
    }

    @Override
    public void noxesium$clearComponents() {
        modified.addAll(values.keySet());
        values.clear();
    }

    /**
     * Returns whether this component holder has no data.
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * Returns whether any values were recently modified and need to be synchronized.
     */
    public boolean hasModified() {
        return !modified.isEmpty();
    }

    /**
     * Removes all noted modifications. This may be used after initialization to clear
     * any artifacts left behind.
     */
    public void clearModifications() {
        modified.clear();
    }

    /**
     * Returns all built up modified values as a patch to send
     * to the client.
     */
    public NoxesiumComponentPatch collectModified(NoxesiumServerPlayer noxesiumPlayer) {
        var data = new HashMap<NoxesiumComponentType<?>, Optional<?>>();
        for (var key : modified) {
            if (!noxesiumPlayer.isAwareOf(registry, key.id())) continue;
            var value = values.get(key);
            if (value == null) {
                data.put(key, Optional.empty());
            } else {
                data.put(key, Optional.of(value));
            }
        }
        return new NoxesiumComponentPatch(data);
    }

    /**
     * Returns all data in this holder.
     */
    public NoxesiumComponentPatch collectAll(NoxesiumServerPlayer noxesiumPlayer) {
        var data = new HashMap<NoxesiumComponentType<?>, Optional<?>>();
        for (var entry : values.entrySet()) {
            if (!noxesiumPlayer.isAwareOf(registry, entry.getKey().id())) continue;
            data.put(entry.getKey(), Optional.of(entry.getValue()));
        }
        return new NoxesiumComponentPatch(data);
    }
}
