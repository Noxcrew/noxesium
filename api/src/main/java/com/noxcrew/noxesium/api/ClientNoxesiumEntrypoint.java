package com.noxcrew.noxesium.api;

/**
 * Extends the {@link NoxesiumEntrypoint} with client-sided methods.
 * Can be defined in `fabric.mod.json` under `noxesium`.
 */
public interface ClientNoxesiumEntrypoint extends NoxesiumEntrypoint {
    /**
     * Returns the protocol version of this entrypoint.
     */
    int getProtocolVersion();

    /**
     * Returns the version of this entrypoint.
     */
    default String getRawVersion() {
        return Integer.toString(getProtocolVersion());
    }

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
