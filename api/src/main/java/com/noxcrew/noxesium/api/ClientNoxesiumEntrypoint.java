package com.noxcrew.noxesium.api;

import java.util.Collection;
import java.util.Collections;
import net.kyori.adventure.key.Key;

/**
 * Extends the {@link NoxesiumEntrypoint} with client-sided methods.
 * Can be defined in `fabric.mod.json` under `noxesium`.
 */
public interface ClientNoxesiumEntrypoint extends NoxesiumEntrypoint {
    /**
     * Returns the capabilities of this entrypoint. This should be a
     * list of unique keys that the server can check against to determine
     * if the client is capable of providing a certain feature.
     * <p>
     * Individual packets and registry entries are automatically synced, but
     * this can be used for more generic concepts.
     */
    default Collection<Key> getCapabilities() {
        return Collections.emptySet();
    }

    /**
     * Returns the version of this entrypoint.
     */
    String getVersion();

    /**
     * Allows implementations to run pre-initialization steps such as registering
     * serializers. This is guaranteed to run on all entrypoints before any are initialized.
     */
    default void preInitialize() {}

    /**
     * Allows implementations to run initialization steps like constructing feature classes.
     * This is guaranteed to run on all entrypoints before any are registered.
     */
    default void initialize() {}
}
