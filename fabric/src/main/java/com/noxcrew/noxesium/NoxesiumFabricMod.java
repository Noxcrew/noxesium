package com.noxcrew.noxesium;

import com.noxcrew.noxesium.feature.CustomServerCreativeItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

/**
 * The main file for the client-side implementation of Noxesium.
 */
public class NoxesiumFabricMod implements ClientModInitializer {

    private static boolean initialized = false;

    /**
     * Initializes Noxesium!
     * Can be called by dependencies to force Noxesium to have finished initializing.
     */
    public static void initialize() {
        if (initialized) return;
        initialized = true;
        new NoxesiumMod(new NoxesiumFabricHook());
        NoxesiumMod.getInstance().registerModule(new CustomServerCreativeItems());
    }

    @Override
    public void onInitializeClient() {
        initialize();

        // Every time the client joins a server we send over information on the version being used,
        // we initialize when both packets are known ad we are in the PLAY phase, whenever both have
        // happened.
        C2SPlayChannelEvents.REGISTER.register((ignored1, ignored2, ignored3, channels) -> {
            NoxesiumMod.getInstance().initialize();
        });
        ClientPlayConnectionEvents.JOIN.register((ignored1, ignored2, ignored3) -> {
            NoxesiumMod.getInstance().initialize();
        });

        // Call disconnection hooks
        ClientPlayConnectionEvents.DISCONNECT.register((ignored1, ignored2) -> {
            NoxesiumMod.getInstance().uninitialize();
        });

        ClientConfigurationConnectionEvents.START.register((ignored1, ignored2) -> {
            // Re-initialize when moving in/out of the config phase, we assume any server
            // running a proxy that doesn't use the configuration phase between servers
            // has their stuff set up well enough to remember the client's information.
            NoxesiumMod.getInstance().uninitialize();
        });
    }
}
