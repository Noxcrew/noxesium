package com.noxcrew.noxesium;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import java.util.Collection;
import java.util.HashSet;
import net.kyori.adventure.key.Key;

/**
 * Holds capabilities provided by the common Noxesium Fabric mod.
 * These can be referenced by servers to detect which major Noxesium systems
 * and features are available for usage.
 */
public class NoxesiumCapabilities {
    /**
     * The namespace used for common Noxesium capabilities.
     */
    private static final String NAMESPACE = NoxesiumReferences.COMMON_ENTRYPOINT;

    /**
     * All capabilities of the common entrypoint.
     */
    public static final Collection<Key> ALL_CAPABILITIES = new HashSet<>();

    /**
     * Whether this client has a full implementation of the original v3.0.0
     * release of Noxesium. Used to distinguish the first full release from
     * any previous betas.
     */
    public static final Key FULL_V3_SUPPORT = register("full_v3_support");

    /**
     * Registers a new capability.
     */
    private static Key register(String id) {
        var key = Key.key(NAMESPACE, id);
        ALL_CAPABILITIES.add(key);
        return key;
    }
}
