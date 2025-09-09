package com.noxcrew.noxesium.api.component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

/**
 * Provides a basic implementation of a mutable component holder.
 */
public class SimpleMutableNoxesiumComponentHolder implements MutableNoxesiumComponentHolder {
    private final Map<NoxesiumComponentType<?>, Object> values = new HashMap<>();
    private Set<NoxesiumComponentType<?>> modified = new HashSet<>();

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
        if (value == null) {
            values.remove(component);
        } else {
            values.put(component, value);
        }
        modified.add(component);
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
    public NoxesiumComponentPatch collectModified() {
        var modified = this.modified;
        this.modified = new HashSet<>();
        var data = new HashMap<NoxesiumComponentType<?>, Optional<?>>();
        for (var key : modified) {
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
    public NoxesiumComponentPatch collectAll() {
        var data = new HashMap<NoxesiumComponentType<?>, Optional<?>>();
        for (var entry : values.entrySet()) {
            data.put(entry.getKey(), Optional.of(entry.getValue()));
        }
        return new NoxesiumComponentPatch(data);
    }
}
