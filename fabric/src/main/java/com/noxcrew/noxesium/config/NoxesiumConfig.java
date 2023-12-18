package com.noxcrew.noxesium.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.noxcrew.noxesium.util.CompatibilityReferences;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

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
    public boolean enableExperimentalPerformancePatches = false;

    // Also create vanilla option instances to use in the video settings menu.
    public OptionInstance<Boolean> vanillaExperimentalPatches = OptionInstance.createBoolean(
            "options.experimental_patches",
            OptionInstance.cachedConstantTooltip(Component.translatable("options.experimental_patches.tooltip")),
            hasConfiguredPerformancePatches(),
            (newValue) -> {
                experimentalPatchesHotkey = newValue;
                save();
            }
    );
    public OptionInstance<Boolean> vanillaFpsOverlay = OptionInstance.createBoolean(
            "options.fps_overlay",
            OptionInstance.cachedConstantTooltip(Component.translatable("options.fps_overlay.tooltip")),
            showFpsOverlay,
            (newValue) -> {
                showFpsOverlay = newValue;
                save();
            }
    );

    /**
     * Returns whether experimental patches are available. This will return false if
     * any mods are detected that are known to have compatibility issues.
     */
    public boolean areExperimentalPatchesAvailable() {
        return !CompatibilityReferences.isUsingFeatherClient();
    }

    /**
     * Returns whether experimental performance are enabled in the configuration.
     */
    public boolean hasConfiguredPerformancePatches() {
        return areExperimentalPatchesAvailable() && enableExperimentalPerformancePatches;
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
