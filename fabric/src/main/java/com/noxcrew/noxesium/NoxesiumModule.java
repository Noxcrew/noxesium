package com.noxcrew.noxesium;

/**
 * The basis for a module in Noxesium that stores some data and needs
 * to be cleared on start-up / shut-down. Contains various hooks useful
 * for determining when data should be determined or cleaned up.
 */
public interface NoxesiumModule {

    /**
     * Called on start-up when the client is being initialised.
     */
    public default void onStartup() {
    }

    /**
     * Called when the client connects to a new server.
     */
    public default void onJoinServer() {
    }

    /**
     * Called when the client disconnects from their current server.
     */
    public default void onQuitServer() {
    }
}
