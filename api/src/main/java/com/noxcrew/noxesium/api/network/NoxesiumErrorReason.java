package com.noxcrew.noxesium.api.network;

/**
 * The different reasons that can cause a handshake to be
 * cancelled/destroyed.
 */
public enum NoxesiumErrorReason {
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
     * The server cancelled the handshake.
     */
    SERVER_CANCELLED,

    /**
     * There was a mismatch between the expected and true handshaking state.
     */
    INVALID_STATE,

    /**
     * The server failed an encryption challenge.
     */
    FAILED_CHALLENGE,

    /**
     * The server does not support any entrypoints, Noxesium will not be able to run.
     */
    NO_MATCHING_ENTRYPOINTS,

    /**
     * The server attempted a transfer when the client was not ready.
     */
    NOT_TRANSFER_READY,
}
