package com.noxcrew.noxesium.feature.ui.cache;

import com.noxcrew.noxesium.NoxesiumMod;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the instances of all element wrappers.
 */
public class ElementManager {

    private static final Map<Class<?>, ElementWrapper> caches = new HashMap<>();

    /**
     * Returns a collection of all created wrappers.
     */
    public static Collection<ElementWrapper> getAllWrappers() {
        return caches.values();
    }

    /**
     * Returns the element of the given type.
     */
    public static <T extends ElementWrapper> T getInstance(Class<T> clazz) {
        var existing = caches.get(clazz);
        if (existing != null) return (T) existing;
        try {
            existing = clazz.newInstance();
        } catch (Exception x) {
            NoxesiumMod.getInstance().getLogger().error("Failed to create new instance of class {}", clazz, x);
        }
        caches.put(clazz, existing);
        return (T) existing;
    }
}
