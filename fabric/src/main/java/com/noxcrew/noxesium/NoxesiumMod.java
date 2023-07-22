package com.noxcrew.noxesium;

import com.noxcrew.noxesium.feature.rule.ServerRule;
import com.noxcrew.noxesium.feature.skull.CustomSkullFont;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.serverbound.ServerboundClientInformationPacket;
import com.noxcrew.noxesium.network.serverbound.ServerboundClientSettingsPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;

/**
 * The main file for the client-side implementation of Noxesium.
 */
public class NoxesiumMod implements ClientModInitializer {

    /**
     * The current protocol version of the mod. Servers can use this version to determine which functionality
     * of Noxesium is available on the client. The protocol version will increment every full release, as such
     * ít is recommended to work with >= comparisons.
     */
    public static final int VERSION = 3;

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
            if (Minecraft.getInstance().getConnection() != null) {
                new ServerboundClientInformationPacket(VERSION).send();
            }

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

        // Register all universal messaging channels
        NoxesiumPackets.registerPackets("universal");
    }

    /**
     * Sends a packet to the server containing the GUI scale of the client which
     * allows servers to more accurately adapt their UI to clients.
     */
    public static void syncGuiScale() {
        // Don't send if there is no established connection
        if (Minecraft.getInstance().getConnection() == null) return;

        var window = Minecraft.getInstance().getWindow();
        var options = Minecraft.getInstance().options;

        new ServerboundClientSettingsPacket(
                options.guiScale().get(),
                window.getGuiScale(),
                window.getGuiScaledWidth(),
                window.getGuiScaledHeight(),
                Minecraft.getInstance().isEnforceUnicode(),
                options.touchscreen().get(),
                options.notificationDisplayTime().get()
        ).send();
    }
}
