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
    default void onStartup() {
    }

    /**
     * Called when a new group of packets is registered.
     * This can be used by other mods to control when to activate
     * themselves.
     */
    default void onGroupRegistered(String group) {
    }

    /**
     * Called when the client connects to a new server.
     */
    default void onJoinServer() {
    }

    /**
     * Called when the client disconnects from their current server.
     */
    default void onQuitServer() {
    }
}
