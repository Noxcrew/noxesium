package com.noxcrew.noxesium;

import com.noxcrew.noxesium.rule.ServerRule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
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
    public static final ResourceLocation CLIENT_SETTINGS_CHANNEL = new ResourceLocation("noxesium", "client_settings");
    public static final ResourceLocation SERVER_RULE_CHANNEL = new ResourceLocation("noxesium", "server_rules");

    public static boolean connected = false;

    @Override
    public void onInitializeClient() {
        // Every time the client joins a server we send over information on the version being used
        ClientPlayConnectionEvents.JOIN.register((ignored1, ignored2, ignored3) -> {
            // Send a packet containing information about the client version of Noxesium
            {
                var outBuffer = PacketByteBufs.create();
                outBuffer.writeInt(VERSION);
                ClientPlayNetworking.send(CLIENT_INFORMATION_CHANNEL, outBuffer);
            }

            // Store that we've connected and are able to send more information
            connected = true;

            // Set up a receiver for any server rules
            ClientPlayNetworking.registerReceiver(SERVER_RULE_CHANNEL, (client, handler, buffer, responseSender) -> {
                ServerRule.readAll(buffer);
            });

            // Inform the player about the GUI scale of the client
            syncGuiScale();
        });

        // Break the connection again on disconnection
        ClientPlayConnectionEvents.DISCONNECT.register((ignored1, ignored2) -> {
            connected = false;

            // Clear all stored server rules
            ServerRule.clearAll();
        });
    }

    /**
     * Sends a packet to the server containing the GUI scale of the client which
     * allows servers to more accurately adapt their UI to clients.
     */
    public static void syncGuiScale() {
        if (!connected) return;
        var outBuffer = PacketByteBufs.create();
        outBuffer.writeInt(Minecraft.getInstance().options.guiScale().get());
        outBuffer.writeBoolean(Minecraft.getInstance().isEnforceUnicode());
        ClientPlayNetworking.send(CLIENT_SETTINGS_CHANNEL, outBuffer);
    }
}
