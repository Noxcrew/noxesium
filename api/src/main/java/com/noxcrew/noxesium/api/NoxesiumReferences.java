package com.noxcrew.noxesium.api;

/**
 * Stores various constant values for Noxesium.
 */
public class NoxesiumReferences {

    /**
     * The current protocol version of the mod. Servers can use this version to determine which functionality
     * of Noxesium is available on the client. The protocol version will increment every full release, as such
     * ít is recommended to work with >= comparisons.
     * <p>
     * Add the versions when this value changes to [NoxesiumListCommand] to serve as a proper record
     * of when it was raised. Protocol version should only raise once per release.
     */
    public static final int VERSION = 18;

    /**
     * The name space to use for Noxesium.
     */
    public static final String NAMESPACE = "noxesium";
}
