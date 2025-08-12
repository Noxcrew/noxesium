package com.noxcrew.noxesium.fabric.network.handshake;

/**
 * The different states of handshaking.
 */
public enum HandshakeState {
    /**
     * No handshake has occurred.
     */
    NONE,

    /**
     * The server has been asked to verify which entrypoints it
     * understands and can handle.
     */
    INITIAL_SERVER_REQUEST,

    /**
     * The handshake has been completed.
     */
    COMPLETE,
}
