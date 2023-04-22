package com.noxcrew.noxesium;

import com.noxcrew.noxesium.rule.ServerRule;
import com.noxcrew.noxesium.skull.CustomSkullFont;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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
    public static final int VERSION = 2;

    public static final ResourceLocation CLIENT_INFORMATION_CHANNEL = new ResourceLocation("noxesium", "client_information");
    public static final ResourceLocation CLIENT_SETTINGS_CHANNEL = new ResourceLocation("noxesium", "client_settings");
    public static final ResourceLocation SERVER_RULE_CHANNEL = new ResourceLocation("noxesium", "server_rules");

    public static final String BUKKIT_COMPOUND_ID = "PublicBukkitValues";
    public static final String IMMOVABLE_TAG = "noxesium:immovable";

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register((ignored1) -> {
            // Create the custom skull font if it's not already created
            CustomSkullFont.createIfNecessary();
        });

        // Every time the client joins a server we send over information on the version being used
        ClientPlayConnectionEvents.JOIN.register((ignored1, ignored2, ignored3) -> {
            // Send a packet containing information about the client version of Noxesium
            {
                if (Minecraft.getInstance().getConnection() != null) {
                    var outBuffer = PacketByteBufs.create();
                    outBuffer.writeInt(VERSION);
                    ClientPlayNetworking.send(CLIENT_INFORMATION_CHANNEL, outBuffer);
                }
            }

            // Set up a receiver for any server rules
            ClientPlayNetworking.registerReceiver(SERVER_RULE_CHANNEL, (client, handler, buffer, responseSender) -> {
                ServerRule.readAll(buffer);
            });

            // Inform the player about the GUI scale of the client
            syncGuiScale();
        });

        // Break the connection again on disconnection
        ClientPlayConnectionEvents.DISCONNECT.register((ignored1, ignored2) -> {
            // Clear all stored server rules
            ServerRule.clearAll();
            CustomSkullFont.clear();
        });
    }

    /**
     * Sends a packet to the server containing the GUI scale of the client which
     * allows servers to more accurately adapt their UI to clients.
     */
    public static void syncGuiScale() {
        if (Minecraft.getInstance().getConnection() == null) return;
        var outBuffer = PacketByteBufs.create();
        outBuffer.writeInt(Minecraft.getInstance().options.guiScale().get());
        outBuffer.writeBoolean(Minecraft.getInstance().isEnforceUnicode());
        // TODO Consider sending the aspect ratio of the game window as well
        ClientPlayNetworking.send(CLIENT_SETTINGS_CHANNEL, outBuffer);
    }
}
