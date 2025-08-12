package com.noxcrew.noxesium.fabric.feature.misc;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import com.noxcrew.noxesium.api.fabric.feature.NoxesiumFeature;
import com.noxcrew.noxesium.fabric.NoxesiumMod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Adds hotkeys for making specific team colors receive glowing outlines.
 */
public class TeamGlowHotkeys implements NoxesiumFeature {

    private static final Map<ChatFormatting, Pair<String, Integer>> GLOW_TEAMS = Map.of(
            ChatFormatting.RED, Pair.of("red", GLFW.GLFW_KEY_KP_7),
            ChatFormatting.GOLD, Pair.of("orange", GLFW.GLFW_KEY_KP_8),
            ChatFormatting.YELLOW, Pair.of("yellow", GLFW.GLFW_KEY_KP_9),
            ChatFormatting.GREEN, Pair.of("lime", GLFW.GLFW_KEY_KP_4),
            ChatFormatting.DARK_GREEN, Pair.of("green", GLFW.GLFW_KEY_KP_5),
            ChatFormatting.DARK_AQUA, Pair.of("cyan", GLFW.GLFW_KEY_KP_6),
            ChatFormatting.AQUA, Pair.of("aqua", GLFW.GLFW_KEY_KP_1),
            ChatFormatting.BLUE, Pair.of("blue", GLFW.GLFW_KEY_KP_2),
            ChatFormatting.DARK_PURPLE, Pair.of("purple", GLFW.GLFW_KEY_KP_3),
            ChatFormatting.LIGHT_PURPLE, Pair.of("pink", GLFW.GLFW_KEY_KP_0));

    private final Map<KeyMapping, Runnable> keybinds = new HashMap<>();
    private final List<ChatFormatting> glowingTeams = new ArrayList<>();

    public TeamGlowHotkeys() {
        // Optionally disable the glowing settings if the config is in use
        if (!NoxesiumMod.getInstance().getConfig().showGlowingSettings) return;

        for (var team : GLOW_TEAMS.entrySet()) {
            register(
                    "key.noxesium.glow." + team.getValue().getFirst(),
                    team.getValue().getSecond(),
                    () -> {
                        if (glowingTeams.contains(team.getKey())) {
                            glowingTeams.remove(team.getKey());
                        } else {
                            glowingTeams.add(team.getKey());
                        }
                    });
        }
        register("key.noxesium.glow.all", GLFW.GLFW_KEY_KP_ADD, () -> glowingTeams.addAll(GLOW_TEAMS.keySet()));
        register("key.noxesium.glow.none", GLFW.GLFW_KEY_KP_SUBTRACT, glowingTeams::clear);
    }

    /**
     * The colors of all teams that should be made to glow.
     *
     * @return a list of team colors
     */
    public List<ChatFormatting> getGlowingTeams() {
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
    public void onRegister() {
        // Set up an end of tick listener to go through each keybind and check for their usage
        ClientTickEvents.END_CLIENT_TICK.register((ignored) -> {
            keybinds.forEach((key, handler) -> {
                while (key.consumeClick()) {
                    handler.run();
                }
            });
        });
    }

    @Override
    public void onUnregister() {
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
