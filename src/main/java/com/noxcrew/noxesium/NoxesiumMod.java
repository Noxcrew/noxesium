package com.noxcrew.noxesium;

import com.noxcrew.noxesium.feature.rule.ServerRule;
import com.noxcrew.noxesium.feature.skull.CustomSkullFont;
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
    public static final int VERSION = 3;

    public static final String API_VERSION = "v1";
    public static final String API_NAMESPACE = "noxesium-" + API_VERSION;

    public static final ResourceLocation CLIENT_INFORMATION_CHANNEL = new ResourceLocation(API_NAMESPACE, "client_information");
    public static final ResourceLocation CLIENT_SETTINGS_CHANNEL = new ResourceLocation(API_NAMESPACE, "client_settings");
    public static final ResourceLocation SERVER_RULE_CHANNEL = new ResourceLocation(API_NAMESPACE, "server_rules");
    public static final ResourceLocation RESET_CHANNEL = new ResourceLocation(API_NAMESPACE, "reset");

    public static final String BUKKIT_COMPOUND_ID = "PublicBukkitValues";
    public static final String NOXESIUM_PREFIX = "noxesium";
    public static final String IMMOVABLE_TAG = NOXESIUM_PREFIX + ":immovable";

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
                    outBuffer.writeVarInt(VERSION);
                    ClientPlayNetworking.send(CLIENT_INFORMATION_CHANNEL, outBuffer);
                }
            }

            // Set up a receiver for any server rules
            ClientPlayNetworking.registerReceiver(SERVER_RULE_CHANNEL, (client, handler, buffer, responseSender) -> {
                ServerRule.readAll(buffer);
            });

            // Set up a receiver for clearing client cached data
            ClientPlayNetworking.registerReceiver(RESET_CHANNEL, (client, handler, buffer, responseSender) -> {
                var command = buffer.readByte();

                if (hasFlag(command, 0)) {
                    ServerRule.clearAll();
                }
                if (hasFlag(command, 1)) {
                    CustomSkullFont.resetCaches();
                }
            });

            // Inform the player about the GUI scale of the client
            syncGuiScale();
        });

        // Break the connection again on disconnection
        ClientPlayConnectionEvents.DISCONNECT.register((ignored1, ignored2) -> {
            // Clear all stored server rules
            ServerRule.clearAll();

            // Clear out all font claims as we can now safely assume
            // we don't need the old ones anymore and there won't be
            // any components that persist between before/after this point
            CustomSkullFont.clearCaches();
        });
    }

    /**
     * Returns whether the flag at index in command is enabled.
     */
    private boolean hasFlag(byte command, int index) {
        return (command & (1 << index)) != 0;
    }

    /**
     * Sends a packet to the server containing the GUI scale of the client which
     * allows servers to more accurately adapt their UI to clients.
     */
    public static void syncGuiScale() {
        if (Minecraft.getInstance().getConnection() == null) return;
        var outBuffer = PacketByteBufs.create();
        var window = Minecraft.getInstance().getWindow();
        var options = Minecraft.getInstance().options;

        outBuffer.writeVarInt(options.guiScale().get());
        outBuffer.writeDouble(window.getGuiScale());
        outBuffer.writeVarInt(window.getGuiScaledWidth());
        outBuffer.writeVarInt(window.getGuiScaledHeight());
        outBuffer.writeBoolean(Minecraft.getInstance().isEnforceUnicode());
        outBuffer.writeBoolean(options.touchscreen().get());
        outBuffer.writeDouble(options.notificationDisplayTime().get());

        ClientPlayNetworking.send(CLIENT_SETTINGS_CHANNEL, outBuffer);
    }
}
