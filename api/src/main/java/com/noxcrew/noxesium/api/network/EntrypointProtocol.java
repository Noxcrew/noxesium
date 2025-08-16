package com.noxcrew.noxesium.api.network;

/**
 * Stores information on an entrypoint's protocol.
 */
public record EntrypointProtocol(String id, int protocolVersion, String rawVersion) {}
