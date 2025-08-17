package com.noxcrew.noxesium.api.registry;

import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.component.RemoteNoxesiumComponentHolder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;

/**
 * Holds the general game component receiver for clients.
 * Placed on a shared object so it does not cause compilation issues on the server side.
 */
public class GameComponents implements RemoteNoxesiumComponentHolder {
    private static final GameComponents INSTANCE = new GameComponents();
    private Map<NoxesiumComponentType<?>, Object> noxesium$components = null;

    /**
     * Returns the main instance of this object.
     */
    public static GameComponents getInstance() {
        return INSTANCE;
    }

    @Override
    public void noxesium$reloadComponents() {
        noxesium$components = null;
    }

    @Override
    public <T> @Nullable T noxesium$getComponent(NoxesiumComponentType<T> component) {
        if (noxesium$components != null) {
            return (T) noxesium$components.get(component);
        }
        return null;
    }

    @Override
    public boolean noxesium$hasComponent(NoxesiumComponentType<?> component) {
        return noxesium$components != null && noxesium$components.containsKey(component);
    }

    @Override
    public void noxesium$loadComponent(NoxesiumComponentType<?> component, Object value) {
        if (value == null) {
            noxesium$unsetComponent(component);
            return;
        }
        if (noxesium$components == null) noxesium$components = new ConcurrentHashMap<>();
        noxesium$components.put(component, value);
    }

    @Override
    public void noxesium$unsetComponent(NoxesiumComponentType<?> component) {
        if (noxesium$components == null) return;
        noxesium$components.remove(component);
        if (noxesium$components.isEmpty()) {
            noxesium$components = null;
        }
    }
}
