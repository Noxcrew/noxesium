package com.noxcrew.noxesium;

import com.mojang.blaze3d.shaders.CompiledShader;
import com.mojang.blaze3d.systems.RenderSystem;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.feature.TeamGlowHotkeys;
import com.noxcrew.noxesium.feature.entity.ExtraEntityDataModule;
import com.noxcrew.noxesium.feature.entity.QibBehaviorModule;
import com.noxcrew.noxesium.feature.entity.SpatialDebuggingModule;
import com.noxcrew.noxesium.feature.model.CustomServerCreativeItems;
import com.noxcrew.noxesium.feature.rule.ServerRuleModule;
import com.noxcrew.noxesium.feature.skull.SkullFontModule;
import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundModule;
import com.noxcrew.noxesium.feature.ui.render.api.NoxesiumRenderStateHolder;
import com.noxcrew.noxesium.network.NoxesiumPacketHandling;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * The main file for the client-side implementation of Noxesium.
 */
public class NoxesiumFabricMod implements ClientModInitializer {

    private static boolean initialized = false;

    /**
     * Initializes Noxesium!
     */
    public static void initialize() {
        if (initialized) return;
        initialized = true;
        NoxesiumMod.configure(new NoxesiumFabricHook());
    }

    @Override
    public void onInitializeClient() {
        // Every time the client joins a server we send over information on the version being used,
        // we initialize when both packets are known ad we are in the PLAY phase, whenever both have
        // happened.
        C2SPlayChannelEvents.REGISTER.register((ignored1, ignored2, ignored3, channels) -> {
            initialize();
        });
        ClientPlayConnectionEvents.JOIN.register((ignored1, ignored2, ignored3) -> {
            initialize();
        });

        // Clear out all UI rendering state when we start configuring
        ClientConfigurationConnectionEvents.START.register((ignored1, ignored2) -> {
            RenderSystem.recordRenderCall(() -> NoxesiumMod.forEachRenderStateHolder(NoxesiumRenderStateHolder::clear));
        });

        // Call disconnection hooks
        ClientPlayConnectionEvents.DISCONNECT.register((ignored1, ignored2) -> {
            NoxesiumMod.getInstance().uninitialize();
        });

        // Hook into client ticking
        ClientTickEvents.START_CLIENT_TICK.register((ignored) -> {
            NoxesiumMod.forEachRenderStateHolder(NoxesiumRenderStateHolder::requestCheck);
        });

        // Re-initialize when moving in/out of the config phase, we assume any server
        // running a proxy that doesn't use the configuration phase between servers
        // has their stuff set up well enough to remember the client's information.
        ClientConfigurationConnectionEvents.START.register((ignored1, ignored2) -> {
            NoxesiumMod.getInstance().uninitialize();
        });

        // Listen to shaders that are loaded and cache them
        ResourceManagerHelper
                .get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(
                        new SimpleResourceReloadListener<Void>() {
                            @Override
                            public ResourceLocation getFabricId() {
                                return ResourceLocation.fromNamespaceAndPath(NoxesiumReferences.NAMESPACE, "shaders");
                            }

                            @Override
                            public CompletableFuture<Void> load(ResourceManager manager, Executor executor) {
                                return NoxesiumMod.cacheShaders(manager);
                            }

                            @Override
                            public CompletableFuture<Void> apply(Void data, ResourceManager manager, Executor executor) {
                                return CompletableFuture.completedFuture(null);
                            }
                        }
                );
    }
}
