package com.noxcrew.noxesium.feature.island;

import com.mojang.blaze3d.platform.InputConstants;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.NoxesiumModule;
import com.noxcrew.noxesium.config.NoxesiumConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks whenever a player has connected to MCC Island and enables
 * specific features.
 */
public class MccIslandTracker implements NoxesiumModule {

    private static final Map<String, Integer> GLOW_TEAMS = Map.of(
            "red", GLFW.GLFW_KEY_KP_7,
            "orange", GLFW.GLFW_KEY_KP_8,
            "yellow", GLFW.GLFW_KEY_KP_9,
            "lime", GLFW.GLFW_KEY_KP_4,
            "green", GLFW.GLFW_KEY_KP_5,
            "cyan", GLFW.GLFW_KEY_KP_6,
            "aqua", GLFW.GLFW_KEY_KP_1,
            "blue", GLFW.GLFW_KEY_KP_2,
            "purple", GLFW.GLFW_KEY_KP_3,
            "pink", GLFW.GLFW_KEY_KP_0
    );

    private final Map<KeyMapping, Runnable> keybinds = new HashMap<>();
    private final List<String> glowingTeams = new ArrayList<>();
    private boolean onMccIsland;

    public MccIslandTracker() {
        // Optionally disable the glowing settings if the config is in use
        if (!NoxesiumMod.getInstance().getConfig().shouldShowGlowingSettings()) return;

        for (var team : GLOW_TEAMS.entrySet()) {
            register(
                    "key.noxesium.glow." + team.getKey(),
                    team.getValue(),
                    () -> {
                        if (glowingTeams.contains(team.getKey())) {
                            glowingTeams.remove(team.getKey());
                        } else {
                            glowingTeams.add(team.getKey());
                        }
                    }
            );
        }
        register(
                "key.noxesium.glow.all",
                GLFW.GLFW_KEY_KP_ADD,
                () -> glowingTeams.addAll(GLOW_TEAMS.keySet())
        );
        register(
                "key.noxesium.glow.none",
                GLFW.GLFW_KEY_KP_SUBTRACT,
                glowingTeams::clear
        );

    }

    /**
     * Marks down that the client is connected to MCC Island.
     */
    public void markOnMccIsland() {
        onMccIsland = true;
    }

    /**
     * Returns whether the client is connected to MCC Island.
     *
     * @return whether the player is connected
     */
    public boolean isOnMccIsland() {
        return onMccIsland;
    }

    /**
     * The ids of all teams that should be made to glow.
     *
     * @return a list of team names
     */
    public List<String> getGlowingTeams() {
        return glowingTeams;
    }

    /**
     * Returns the id of the keybind category used for custom keybinds.
     *
     * @return the keybind category id
     */
    public String getKeybindCategory() {
        return "category.noxesium";
    }

    @Override
    public void onStartup() {
        // Set up an end of tick listener to go through each keybind and check for their usage
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            keybinds.forEach((key, handler) -> {
                while (key.consumeClick()) {
                    handler.run();
                }
            });
        });
    }

    @Override
    public void onQuitServer() {
        onMccIsland = false;
        glowingTeams.clear();
    }

    /**
     * Registers a new key binding with the given handler.
     */
    private void register(String translationKey, int code, Runnable handler) {
        var key = new KeyMapping(translationKey, InputConstants.Type.KEYSYM, code, getKeybindCategory());
        KeyBindingHelper.registerKeyBinding(key);
        keybinds.put(key, handler);
    }
}
