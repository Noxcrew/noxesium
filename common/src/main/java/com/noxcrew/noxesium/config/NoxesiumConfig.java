package com.noxcrew.noxesium.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.api.config.BooleanOrDefault;
import com.noxcrew.noxesium.api.config.MapLocation;
import com.noxcrew.noxesium.feature.rule.ServerRules;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Stores Noxesium's configured values.
 */
public class NoxesiumConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean resetToggleKeys = false;
    public BooleanOrDefault renderMapsInUi = BooleanOrDefault.DEFAULT;
    public boolean showFpsOverlay = false;
    public boolean showGameTimeOverlay = false;
    public boolean enableQibSystemDebugging = false;
    public boolean showGlowingSettings = false;
    public boolean dumpIncomingPackets = false;
    public boolean dumpOutgoingPackets = false;
    public boolean printPacketExceptions = false;
    public double mapUiSize = 0.8;
    public MapLocation mapUiLocation = MapLocation.TOP;

    /**
     * Returns whether to render maps in the UI.
     */
    public boolean shouldRenderMapsInUi() {
        if (renderMapsInUi == BooleanOrDefault.DEFAULT) {
            return ServerRules.SHOW_MAP_IN_UI.getValue();
        }
        return renderMapsInUi == BooleanOrDefault.TRUE;
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
        return NoxesiumMod.getPlatform().getConfigDirectory().resolve("noxesium-config.json");
    }
}
