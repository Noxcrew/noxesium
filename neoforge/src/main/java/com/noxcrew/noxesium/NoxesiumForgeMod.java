package com.noxcrew.noxesium;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.config.NoxesiumSettingsScreen;
import com.noxcrew.noxesium.feature.CustomServerCreativeItems;
import com.noxcrew.noxesium.feature.ui.render.api.NoxesiumRenderStateHolder;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Defines the main mod for NeoForge.
 */
@Mod(value = "noxesium", dist = Dist.CLIENT)
public class NoxesiumForgeMod {

    public NoxesiumForgeMod(ModContainer container) {
        // Register the configuration menu accessible from the mods menu
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, screen) -> new NoxesiumSettingsScreen(screen));

        // Register this file as a listener
        NeoForge.EVENT_BUS.register(this);

        // Register a listener on our own mod bus for the reload listeners
        container.getEventBus().addListener(RegisterClientReloadListenersEvent.class, this::registerReloadListeners);

        // Set up the NoxesiumMod
        new NoxesiumMod(new NoxesiumForgeHook(container));

        // Add the custom creative tab for server items
        var creativeTab = new CustomServerCreativeItems();
        NeoForge.EVENT_BUS.register(creativeTab);
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

    /**
     * Hook into client ticking for the UI optimizations.
     */
    @SubscribeEvent
    public void onTickStart(ClientTickEvent.Pre event) {
        NoxesiumMod.forEachRenderStateHolder(NoxesiumRenderStateHolder::requestCheck);
    }

    /**
     * Hook into client reloading for shader caching.
     */
    public void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new PreparableReloadListener() {
            @Override
            public String getName() {
                return NoxesiumReferences.NAMESPACE + ":shaders";
            }

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, Executor executor, Executor executor1) {
                return NoxesiumMod.cacheShaders(resourceManager);
            }
        });
    }
}
