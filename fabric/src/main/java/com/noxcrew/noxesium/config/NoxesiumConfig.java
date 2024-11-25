package com.noxcrew.noxesium.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.noxcrew.noxesium.api.config.MapLocation;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Stores Noxesium's configured values.
 */
public class NoxesiumConfig {

    /**
     * The current state of the hotkey for toggling the experimental
     * patches on/off.
     */
    public static Boolean experimentalPatchesHotkey = null;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean resetToggleKeys = false;
    public TriState renderMapsInUi = TriState.DEFAULT;
    public boolean showFpsOverlay = false;
    public boolean showGameTimeOverlay = false;
    public boolean enableQibSystemDebugging = false;
    public boolean disableExperimentalPerformancePatches = true; // Still off by default until thoroughly tested!
    public boolean showGlowingSettings = false;
    public boolean dumpIncomingPackets = false;
    public boolean dumpOutgoingPackets = false;
    public boolean printPacketExceptions = false;
    public double mapUiSize = 0.8;
    public MapLocation mapUiLocation = MapLocation.TOP;
    public int maxUiFramerate = 60;
    public boolean showOptimizationOverlay = false;

    /**
     * Returns whether experimental performance are enabled in the configuration.
     */
    public boolean hasConfiguredPerformancePatches() {
        return !disableExperimentalPerformancePatches;
    }

    /**
     * Returns whether to render maps in the UI.
     */
    public boolean shouldRenderMapsInUi() {
        if (renderMapsInUi == TriState.DEFAULT) {
            return ServerRules.SHOW_MAP_IN_UI.getValue();
        }
        return renderMapsInUi.get();
    }

    /**
     * Whether the experimental performance patches should be used.
     */
    public boolean shouldDisableExperimentalPerformancePatches() {
        if (ServerRules.DISABLE_UI_OPTIMIZATIONS.getValue()) return true;

        if (hasConfiguredPerformancePatches()) {
            if (experimentalPatchesHotkey != null) {
                return !experimentalPatchesHotkey;
            }
            return false;
        }
        return true;
    }

    /**
     * Loads this configuration file.
     */
    public static NoxesiumConfig load() {
        var file = getConfigFile();
        if (Files.exists(file)) {
            try (var reader = new FileReader(file.toFile())) {
                return GSON.fromJson(reader, NoxesiumConfig.class);
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return new NoxesiumConfig();
    }

    /**
     * Saves the changes to this configuration file.
     */
    public void save() {
        try {
            Files.writeString(getConfigFile(), GSON.toJson(this));
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    /**
     * Returns the path where the configuration file is stored.
     */
    public static Path getConfigFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("noxesium-config.json");
    }
}
