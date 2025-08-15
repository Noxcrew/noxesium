package com.noxcrew.noxesium.api.network;

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
    AWAITING_RESPONSE,

    /**
     * The handshake has been completed.
     */
    COMPLETE,
}
