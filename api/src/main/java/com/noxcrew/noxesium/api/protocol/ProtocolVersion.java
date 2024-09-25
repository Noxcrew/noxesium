package com.noxcrew.noxesium.api.protocol;

/**
 * Stores information about protocol versions.
 */
public class ProtocolVersion {

    /**
     * The current protocol version of the mod. Servers can use this version to determine which functionality
     * of Noxesium is available on the client. The protocol version will increment every full release, as such
     * ít is recommended to work with >= comparisons.
     */
    public static final int VERSION = 11;

    /**
     * The name space to use for Noxesium.
     */
    public static final String NAMESPACE = "noxesium";
}
