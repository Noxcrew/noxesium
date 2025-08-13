package com.noxcrew.noxesium.fabric;

import com.noxcrew.noxesium.api.fabric.NoxesiumApi;
import com.noxcrew.noxesium.api.fabric.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.fabric.network.NoxesiumNetworking;
import com.noxcrew.noxesium.fabric.config.NoxesiumConfig;
import com.noxcrew.noxesium.fabric.feature.entity.SpatialInteractionEntityTree;
import com.noxcrew.noxesium.fabric.feature.misc.CustomServerCreativeItems;
import com.noxcrew.noxesium.fabric.feature.render.CustomRenderTypes;
import com.noxcrew.noxesium.fabric.feature.skull.SkullFontModule;
import com.noxcrew.noxesium.fabric.network.NoxesiumInitializer;
import com.noxcrew.noxesium.fabric.network.serverbound.ServerboundMouseButtonClickPacket;
import com.noxcrew.noxesium.fabric.registry.CommonBlockEntityComponentTypes;
import com.noxcrew.noxesium.fabric.registry.CommonEntityComponentTypes;
import com.noxcrew.noxesium.fabric.registry.CommonGameComponentTypes;
import com.noxcrew.noxesium.fabric.registry.CommonItemComponentTypes;
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

        // Create the custom creative tab at start-up as it uses a registry
        new CustomServerCreativeItems();

        // Set the packet dumping values which are needed by the API
        NoxesiumNetworking.dumpIncomingPackets = config.getDumpIncomingPackets();
        NoxesiumNetworking.dumpOutgoingPackets = config.getDumpOutgoingPackets();
    }

    @Override
    public void onInitializeClient() {
        // Go through all entrypoints and register them
        var logger = NoxesiumApi.getLogger();
        var api = NoxesiumApi.getInstance();
        FabricLoader.getInstance()
                .getEntrypointContainers("noxesium", NoxesiumEntrypoint.class)
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
        new NoxesiumInitializer().register();

        // Trigger registration of all registries
        Object ignored = CommonEntityComponentTypes.BEAM_COLOR;
        ignored = CommonGameComponentTypes.DISABLE_SPIN_ATTACK_COLLISIONS;
        ignored = CommonItemComponentTypes.HOVER_SOUND;
        ignored = CommonBlockEntityComponentTypes.BEACON_BEAM_HEIGHT;
        ignored = CustomRenderTypes.linesNoDepth();

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
