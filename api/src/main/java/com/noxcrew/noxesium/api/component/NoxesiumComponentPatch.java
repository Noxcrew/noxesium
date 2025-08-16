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
}
