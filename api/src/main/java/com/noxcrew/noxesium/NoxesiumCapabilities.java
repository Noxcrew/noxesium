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

    // There are no capabilities yet because the API is brand new... But here's some examples!
    public static final Key V3_API_SUPPORT = register("v3_api_support");
    public static final Key ANONYMOUS_PLAYER_HEADS = register("anonymous_player_heads");
    public static final Key FUNCTIONAL_QIB_SYSTEM = register("functional_qib_system");

    /**
     * Registers a new capability.
     */
    private static Key register(String id) {
        var key = Key.key(NAMESPACE, id);
        ALL_CAPABILITIES.add(key);
        return key;
    }
}
