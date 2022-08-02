package com.noxcrew.noxesium;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.resources.ResourceLocation;

/**
 * The main file for the client-side implementation of Noxesium.
 */
public class NoxesiumMod implements ClientModInitializer {

    /**
     * The current protocol version of the mod. Servers can use this version to determine which functionality
     * of Noxesium is available on the client.
     */
    public static final int VERSION = 0;

    public static final ResourceLocation CLIENT_INFORMATION_CHANNEL = new ResourceLocation("noxesium", "client_information");

    @Override
    public void onInitializeClient() {
        // Every time the client joins a server we send over information on the version being used
        ClientPlayConnectionEvents.JOIN.register((ignored1, ignored2, ignored3) -> {
            var outBuffer = PacketByteBufs.create();
            outBuffer.writeInt(VERSION);
            ClientPlayNetworking.send(CLIENT_INFORMATION_CHANNEL, outBuffer);
        });
    }
}
