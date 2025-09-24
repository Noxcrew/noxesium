package com.noxcrew.noxesium.api.network;

/**
 * The different reasons that can cause a handshake to be
 * cancelled/destroyed.
 */
public enum NoxesiumErrorReason {
    // ### CLIENT REASONS
    /**
     * The client has disconnected from the server and is no longer in any
     * phase.
     */
    DISCONNECT,

    /**
     * The server has indicated it no longer supports Noxesium's plugin channels.
     */
    CHANNEL_UNREGISTERED,

    /**
     * The server failed an encryption challenge.
     */
    FAILED_CHALLENGE,

    /**
     * The server attempted a transfer when the client was not ready.
     */
    NOT_TRANSFER_READY,

    // ### SERVER REASONS
    /**
     * No information is known about the relevant client.
     */
    CLIENT_UNKNOWN,

    /**
     * The server made a logical mistake and the client should re-authenticate.
     */
    SERVER_ERROR,

    // ### SHARED REASONS
    /**
     * The other side does not support any entrypoints, Noxesium will not be able to run.
     */
    NO_MATCHING_ENTRYPOINTS,

    /**
     * There was a mismatch between the expected and true handshaking state.
     */
    INVALID_STATE,

    /**
     * There was no response within a reasonable time.
     */
    TIMEOUT,
}
