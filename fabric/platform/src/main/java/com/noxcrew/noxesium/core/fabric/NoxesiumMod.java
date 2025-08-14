package com.noxcrew.noxesium.core.fabric;

import com.noxcrew.noxesium.api.NoxesiumApi;
import com.noxcrew.noxesium.api.nms.NmsNoxesiumEntrypoint;
import com.noxcrew.noxesium.api.nms.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.core.fabric.config.NoxesiumConfig;
import com.noxcrew.noxesium.core.fabric.feature.entity.SpatialInteractionEntityTree;
import com.noxcrew.noxesium.core.fabric.feature.misc.CustomServerCreativeItems;
import com.noxcrew.noxesium.core.fabric.feature.skull.SkullFontModule;
import com.noxcrew.noxesium.core.fabric.network.FabricNoxesiumServerboundNetworking;
import com.noxcrew.noxesium.core.fabric.network.NoxesiumClientHandshaker;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundMouseButtonClickPacket;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

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
    private final SkullFontModule skullFontModule;
    private final CustomServerCreativeItems customCreativeItems;

    /**
     * If enabled settings are not overridden. This should be true while rendering the settings menu.
     */
    public boolean disableSettingOverrides = false;

    /**
     * Whether Iris is being used. If true we don't allow the graphics setting to be changed to Fabulous! as
     * to not break Iris.
     */
    public boolean isUsingIris = false;

    /**
     * Whether the creative tab has changed.
     */
    public boolean hasCreativeTabChanged = false;

    /**
     * The types of button clicks that have already been sent this tick.
     */
    public Set<ServerboundMouseButtonClickPacket.Button> sentButtonClicks = new HashSet<>();

    /**
     * Creates a new NoxesiumMod instance.
     */
    public NoxesiumMod() {
        instance = this;
        config = NoxesiumConfig.load();
        skullFontModule = new SkullFontModule();
        customCreativeItems = new CustomServerCreativeItems();
        NoxesiumServerboundNetworking.setInstance(new FabricNoxesiumServerboundNetworking());
    }

    @Override
    public void onInitializeClient() {
        // Go through all entrypoints and register them
        var logger = NoxesiumApi.getLogger();
        var api = NoxesiumApi.getInstance();
        FabricLoader.getInstance()
                .getEntrypointContainers("noxesium", NmsNoxesiumEntrypoint.class)
                .forEach(entrypoint -> {
                    try {
                        api.registerEndpoint(entrypoint.getEntrypoint());
                    } catch (Exception e) {
                        logger.error(
                                "Failed to initialize Noxesium entrypoint from mod {}",
                                entrypoint.getProvider().getMetadata().getId());
                    }
                });

        // Log how many entrypoints were successfully loaded
        logger.info("Loaded {} extensions to Noxesium", api.getAllEntrypoints().size());

        // Set up the initializer
        new NoxesiumClientHandshaker().register();

        // Run rebuilds on a separate thread to not destroy fps unnecessarily.
        var backgroundTaskThread = new Thread("Noxesium Background Task Thread") {
            @Override
            public void run() {
                while (true) {
                    try {
                        SpatialInteractionEntityTree.rebuild();
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

        // Determine if Iris is present or not
        isUsingIris = FabricLoader.getInstance().isModLoaded("iris");

        // Clear the packet list every tick
        ClientTickEvents.END_CLIENT_TICK.register((ignored2) -> {
            if (!sentButtonClicks.isEmpty()) {
                sentButtonClicks.clear();
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
     * Returns the skull font module.
     */
    public SkullFontModule getSkullFontModule() {
        return skullFontModule;
    }
}
