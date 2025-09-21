package com.noxcrew.noxesium.api;

/**
 * Stores various constant values for Noxesium.
 */
public class NoxesiumReferences {
    /**
     * The name space to use for Noxesium.
     */
    public static final String NAMESPACE = "noxesium";

    /**
     * The custom namespace to use for component metadata.
     */
    public static final String COMPONENT_NAMESPACE = "NoxesiumData";

    /**
     * The namespace under which all packets are registered.
     * Appended by a global API version equal to the major version of Noxesium.
     */
    // TODO Remove the alpha tag to restore the correct namespace!
    public static final String PACKET_NAMESPACE = NAMESPACE + "-v3-alpha5";

    /**
     * The identifier used by the common entrypoint.
     */
    public static final String COMMON_ENTRYPOINT = "noxesium-common";
}
