package com.noxcrew.noxesium.core.fabric.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.api.util.BooleanOrDefault;
import com.noxcrew.noxesium.core.feature.GuiElement;
import com.noxcrew.noxesium.core.feature.MapLocation;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.permissions.Permissions;

/**
 * Stores Noxesium's configured values.
 */
public class NoxesiumConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean resetToggleKeys = false;
    public BooleanOrDefault renderMapsInUi = BooleanOrDefault.DEFAULT;
    public boolean showGameTimeOverlay = false;
    public boolean enableQibSystemDebugging = false;
    public boolean dumpIncomingPackets = false;
    public boolean dumpOutgoingPackets = false;
    public boolean printPacketExceptions = false;
    public boolean debugScoreboardTeams = false;
    public boolean showCullingBoxes = false;
    public MapLocation mapUiLocation = MapLocation.TOP;
    public double bossBarPosition = 0.0;
    public double scoreboardPosition = 0.0;
    public double mapPosition = -1.0;
    private Map<GuiElement, Double> scales;

    /**
     * Whether to show Qib system debugging visuals.
     */
    public boolean showQibSystemDebugging() {
        return hasOperatorPermissions() && enableQibSystemDebugging;
    }

    /**
     * Whether to show culling hitboxes.
     */
    public boolean showCullingBoxes() {
        return hasOperatorPermissions() && showCullingBoxes;
    }

    /**
     * Returns whether the player has operator permissions.
     */
    public boolean hasOperatorPermissions() {
        return Minecraft.getInstance().player != null
                && Minecraft.getInstance().player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    /**
     * Returns the scales map.
     */
    public Map<GuiElement, Double> getScales() {
        if (scales == null) {
            scales = new HashMap<>();

            // Legacy load old position setting!
            if (mapUiLocation.isBottom()) {
                mapPosition = 1.0;
            }
        }
        return scales;
    }

    /**
     * Returns the scale of the given element.
     */
    public double getScale(GuiElement element) {
        var rawValue = getScales().getOrDefault(element, 1.0);
        var map = GameComponents.getInstance().noxesium$getOptionalComponent(CommonGameComponentTypes.GUI_CONSTRAINTS);
        if (map.isPresent()) {
            var constraints = map.get().get(element);
            if (constraints != null) {
                var value = rawValue * constraints.scalar();
                return Math.clamp(value, constraints.minValue(), constraints.maxValue());
            }
        }
        return Math.clamp(rawValue, 0.001, 100.0);
    }

    /**
     * Returns whether to render maps in the UI.
     */
    public boolean shouldRenderMapsInUi() {
        var showMapUi = GameComponents.getInstance().noxesium$getComponent(CommonGameComponentTypes.SHOW_MAP_IN_UI);
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
