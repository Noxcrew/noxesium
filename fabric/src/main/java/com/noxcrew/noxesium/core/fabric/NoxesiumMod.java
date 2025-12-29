package com.noxcrew.noxesium.core.fabric;

import com.noxcrew.noxesium.api.ClientNoxesiumEntrypoint;
import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.NoxesiumSide;
import com.noxcrew.noxesium.api.feature.sprite.CustomSkullSprite;
import com.noxcrew.noxesium.api.feature.sprite.CustomSpriteRegistry;
import com.noxcrew.noxesium.api.network.NoxesiumNetworking;
import com.noxcrew.noxesium.api.nms.NoxesiumPlatform;
import com.noxcrew.noxesium.core.fabric.config.NoxesiumConfig;
import com.noxcrew.noxesium.core.fabric.feature.CustomServerCreativeItems;
import com.noxcrew.noxesium.core.fabric.network.FabricNoxesiumClientHandshaker;
import com.noxcrew.noxesium.core.fabric.network.FabricNoxesiumServerboundNetworking;
import com.noxcrew.noxesium.core.fabric.util.BackgroundTaskFeature;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundMouseButtonClickPacket;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

/**
 * The base for the fabric mod which loads in all entrypoints and manages registries.
 */
public class NoxesiumMod implements ClientModInitializer {
    private static NoxesiumMod instance;

    /**
     * Returns the known Noxesium instance.
     */
    public static NoxesiumMod getInstance() {
        return instance;
    }

    private final NoxesiumConfig config;
    private CustomServerCreativeItems customCreativeItems;

    /**
     * Whether the creative tab has changed.
     */
    public boolean hasCreativeTabChanged = false;

    /**
     * The types of button clicks that have already been sent this tick.
     */
    public Set<ServerboundMouseButtonClickPacket.Button> sentButtonClicks = new HashSet<>();

    /**
     * The handler of the client-side handshake process.
     */
    @Nullable
    private FabricNoxesiumClientHandshaker handshaker;

    /**
     * All pending tasks to run on the main thread.
     */
    private final Queue<Runnable> mainThreadTasks = new ConcurrentLinkedQueue();

    /**
     * Creates a new NoxesiumMod instance.
     */
    public NoxesiumMod() {
        instance = this;
        config = NoxesiumConfig.load();
        NoxesiumApi.getInstance().setSide(NoxesiumSide.CLIENT);
        NoxesiumPlatform.setInstance(new FabricPlatform());
        NoxesiumNetworking.setInstance(new FabricNoxesiumServerboundNetworking());
    }

    @Override
    public void onInitializeClient() {
        // Initialize creative tab when the client is ready
        customCreativeItems = new CustomServerCreativeItems();

        // Go through all entrypoints and register them
        var logger = NoxesiumApi.getLogger();
        var api = NoxesiumApi.getInstance();
        var entrypoints =
                FabricLoader.getInstance().getEntrypointContainers("noxesium", ClientNoxesiumEntrypoint.class);
        entrypoints.forEach(entrypoint -> entrypoint.getEntrypoint().preInitialize());
        entrypoints.forEach(entrypoint -> entrypoint.getEntrypoint().initialize());
        entrypoints.forEach(entrypoint -> api.registerEntrypoint(entrypoint.getEntrypoint()));

        // Log how many entrypoints were successfully loaded
        logger.info("Loaded {} Noxesium entrypoints", api.getAllEntrypoints().size());

        // Register supported custom sprite types
        CustomSpriteRegistry.register("skull", CustomSkullSprite::deserialize);

        // Set up the initializer
        handshaker = new FabricNoxesiumClientHandshaker();
        handshaker.register();

        // Run rebuilds on a separate thread to not destroy fps unnecessarily.
        var backgroundTaskThread = new Thread("Noxesium Background Task Thread") {
            @Override
            public void run() {
                while (true) {
                    try {
                        NoxesiumApi.getInstance().getAllFeatures().forEach(feature -> {
                            if (feature instanceof BackgroundTaskFeature backgroundTaskFeature) {
                                backgroundTaskFeature.runAsync();
                            }
                        });
                        Thread.sleep(20);
                    } catch (InterruptedException ex) {
                        return;
                    } catch (Exception ex) {
                        logger.error("Caught exception from Noxesium Background Task Thread", ex);
                    }
                }
            }
        };
        backgroundTaskThread.setDaemon(true);
        backgroundTaskThread.start();

        ClientTickEvents.END_CLIENT_TICK.register((ignored2) -> {
            // Clear the packet list every tick
            if (!sentButtonClicks.isEmpty()) {
                sentButtonClicks.clear();
            }

            // Run main thread tasks
            Runnable task;
            while ((task = mainThreadTasks.poll()) != null) {
                task.run();
            }
        });
    }

    /**
     * Returns the configuration used by Noxesium.
     */
    public NoxesiumConfig getConfig() {
        return config;
    }

    /**
     * Returns the module in charge of handshaking with the server.
     */
    @Nullable
    public FabricNoxesiumClientHandshaker getHandshaker() {
        return handshaker;
    }

    /**
     * Returns the custom creative tab.
     */
    public CustomServerCreativeItems getCustomCreativeItems() {
        return customCreativeItems;
    }

    /**
     * Runs runnable on the main thread.
     */
    public void ensureMain(Runnable runnable) {
        mainThreadTasks.add(runnable);
    }
}
