package com.noxcrew.noxesium.core.fabric.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.noxcrew.noxesium.api.fabric.network.NoxesiumNetworking;
import com.noxcrew.noxesium.api.util.BooleanOrDefault;
import com.noxcrew.noxesium.core.client.setting.MapLocation;
import com.noxcrew.noxesium.core.fabric.registry.CommonGameComponentTypes;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

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
    public boolean printPacketExceptions = false;
    public boolean debugScoreboardTeams = false;
    public boolean showCullingBoxes = false;
    public double mapUiSize = 0.8;
    public MapLocation mapUiLocation = MapLocation.TOP;
    private boolean dumpIncomingPackets = false;
    private boolean dumpOutgoingPackets = false;

    /**
     * Whether to dump all incoming packets.
     */
    public boolean getDumpIncomingPackets() {
        return dumpIncomingPackets;
    }

    /**
     * Whether to dump all outgoing packets.
     */
    public boolean getDumpOutgoingPackets() {
        return dumpOutgoingPackets;
    }

    /**
     * Updates whether incoming packets should be dumped.
     */
    public void setDumpIncomingPackets(boolean value) {
        this.dumpIncomingPackets = value;
        NoxesiumNetworking.dumpIncomingPackets = value;
    }

    /**
     * Updates whether outgoing packets should be dumped.
     */
    public void setDumpOutgoingPackets(boolean value) {
        this.dumpOutgoingPackets = value;
        NoxesiumNetworking.dumpOutgoingPackets = value;
    }

    /**
     * Returns whether to render maps in the UI.
     */
    public boolean shouldRenderMapsInUi() {
        var showMapUi = Minecraft.getInstance().noxesium$getComponent(CommonGameComponentTypes.SHOW_MAP_IN_UI);
        if (Objects.equals(showMapUi, false)) {
            // If the server has disabled the map, never show it!
            return false;
        }
        if (renderMapsInUi == BooleanOrDefault.DEFAULT) {
            // If we match what the server wants, then match that!
            return showMapUi != null && showMapUi;
        }

        // Otherwise use the client setting
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
        return FabricLoader.getInstance().getConfigDir().resolve("noxesium-config.json");
    }
}
