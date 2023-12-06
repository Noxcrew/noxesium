package com.noxcrew.noxesium;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.noxcrew.noxesium.util.CompatibilityReferences;
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

    public boolean showFpsOverlay = false;
    private boolean enableExperimentalPerformancePatches = false;

    /**
     * Changes the current configured state of the experimental performance patches.
     */
    public void setEnableExperimentalPerformancePatches(boolean value) {
        // Never allow the custom patches when using feather
        if (CompatibilityReferences.isUsingFeatherClient()) {
            this.enableExperimentalPerformancePatches = false;
            return;
        }
        this.enableExperimentalPerformancePatches = value;
    }

    /**
     * Returns whether experimental performance are enabled in the configuration.
     */
    public boolean hasConfiguredPerformancePatches() {
        // Never allow the custom patches when using feather
        if (CompatibilityReferences.isUsingFeatherClient()) {
            return false;
        }
        return enableExperimentalPerformancePatches;
    }

    /**
     * Whether the experimental performance patches should be used.
     */
    public boolean shouldDisableExperimentalPerformancePatches() {
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
