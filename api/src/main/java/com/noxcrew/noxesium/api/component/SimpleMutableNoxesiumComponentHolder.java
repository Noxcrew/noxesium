package com.noxcrew.noxesium.api.component;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

/**
 * Provides a basic implementation of a mutable component holder.
 */
public class SimpleMutableNoxesiumComponentHolder implements MutableNoxesiumComponentHolder {
    private final Map<NoxesiumComponentType<?>, Object> values = new HashMap<>();

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
        values.put(component, value);
    }

    @Override
    public void noxesium$clearComponents() {
        values.clear();
    }
}
