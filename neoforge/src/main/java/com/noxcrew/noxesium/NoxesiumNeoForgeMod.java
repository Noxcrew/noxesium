package com.noxcrew.noxesium;

import com.noxcrew.noxesium.config.NoxesiumSettingsScreen;
import com.noxcrew.noxesium.feature.CustomServerCreativeItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Defines the main mod for NeoForge.
 */
@Mod(value = "noxesium", dist = Dist.CLIENT)
public class NoxesiumNeoForgeMod {

    public NoxesiumNeoForgeMod(ModContainer container) {
        // Register the configuration menu accessible from the mods menu
        container.registerExtensionPoint(
                IConfigScreenFactory.class, (modContainer, screen) -> new NoxesiumSettingsScreen(screen));

        // Register this file as a listener
        NeoForge.EVENT_BUS.register(this);

        // Set up the NoxesiumMod
        new NoxesiumMod(new NoxesiumForgeHook(container));

        // Add the custom creative tab for server items
        var creativeTab = new CustomServerCreativeItems();
        container.getEventBus().register(creativeTab);
        NoxesiumMod.getInstance().registerModule(creativeTab);
    }

    /**
     * Set up receivers whenever you join a server.
     */
    @SubscribeEvent
    public void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        NoxesiumMod.getInstance().initialize();
    }

    /**
     * Break down registered channels when leaving a server.
     */
    @SubscribeEvent
    public void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        NoxesiumMod.getInstance().uninitialize();
    }
}
