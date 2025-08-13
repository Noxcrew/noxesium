package com.noxcrew.noxesium.api.component;

import java.util.Map;
import java.util.Optional;

/**
 * Stores a collection of components to be applied to or removed from a holder.
 */
public class NoxesiumComponentPatch {
    private final Map<NoxesiumComponentType<?>, Optional<?>> data;

    public NoxesiumComponentPatch(Map<NoxesiumComponentType<?>, Optional<?>> data) {
        this.data = data;
    }

    /**
     * Returns whether this patch is empty.
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Returns the raw contents of this patch.
     */
    public Map<NoxesiumComponentType<?>, Optional<?>> getMap() {
        return data;
    }

    /**
     * Applies this patch to the given holder.
     */
    public void apply(NoxesiumComponentHolder holder) {
        for (var entry : data.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            if (value.isEmpty()) {
                if (key.listener() != null && key.listener().hasListeners()) {
                    var oldValue = holder.noxesium$getComponent(key);
                    holder.noxesium$unsetComponent(key);
                    key.listener().trigger(holder, oldValue, null);
                } else {
                    holder.noxesium$unsetComponent(key);
                }
            } else {
                if (key.listener() != null && key.listener().hasListeners()) {
                    var oldValue = holder.noxesium$getComponent(key);
                    var newValue = value.orElse(null);
                    holder.noxesium$loadComponent(key, newValue);
                    key.listener().trigger(holder, oldValue, newValue);
                } else {
                    holder.noxesium$loadComponent(key, value.get());
                }
            }
        }
    }
}
