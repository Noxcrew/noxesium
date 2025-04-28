package com.noxcrew.noxesium.api.util;

import org.jetbrains.annotations.Nullable;

/**
 * Provides an enum storing all available debug options and their corresponding keycodes.
 * This should be used in correspondence with the RESTRICT_DEBUG_OPTIONS ServerRule.
 */
public enum DebugOption {
    TOGGLE_PROFILER(49, null),
    TOGGLE_FPS_CHARTS(50, null),
    TOGGLE_NETWORK_CHARTS(51, null),
    RELOAD_CHUNKS(65, "debug.reload_chunks.help"),
    SHOW_HITBOXES(66, "debug.show_hitboxes.help"),
    COPY_LOCATION(67, "debug.copy_location.help"),
    CLEAR_CHAT(68, "debug.clear_chat.help"),
    CHUNK_BOUNDARIES(71, "debug.chunk_boundaries.help"),
    ADVANCED_TOOLTIPS(72, "debug.advanced_tooltips.help"),
    INSPECT(73, "debug.inspect.help"),
    SMART_CULL(76, "debug.profiling.help"),
    CREATIVE_SPECTATOR(78, "debug.creative_spectator.help"),
    PAUSE_ON_LOST_FOCUS(80, "debug.pause_focus.help"),
    HELP(81, "debug.help.help"),
    DUMP_TEXTURES(83, "debug.dump_dynamic_textures.help"),
    RELOAD_RESOURCES(84, "debug.reload_resourcepacks.help"),
    NOXESIUM_SETTINGS(86, "debug.noxesium_settings.help"),
    ESCAPE(256, "debug.pause.help"),
    GAME_MODE_SWITCHER(293, "debug.gamemodes.help");

    private final int keyCode;
    private final @Nullable String translationKey;

    /**
     * Constructs a new debug option.
     *
     * @param keyCode             The key code associated with this debug option.
     * @param helpTranslationKey  The translation key used by Minecraft.
     */
    DebugOption(int keyCode, @Nullable String helpTranslationKey) {
        this.keyCode = keyCode;
        this.translationKey = helpTranslationKey;
    }

    /**
     * Gets the key code associated with this debug option.
     *
     * @return The key code integer.
     */
    public int getKeyCode() {
        return keyCode;
    }

    /**
     * Gets the translation key for this debug option's help text.
     *
     * @return The translation key, or null if no help text is available.
     */
    public @Nullable String getTranslationKey() {
        return translationKey;
    }

    /**
     * Finds a debug option by its translation key.
     *
     * @param translationKey The translation key to search for.
     * @return The matching debug option, or null if none is found.
     */
    public static @Nullable DebugOption getByTranslationKey(String translationKey) {
        for (DebugOption key : values()) {
            if (key.translationKey != null && key.translationKey.equals(translationKey)) {
                return key;
            }
        }
        return null;
    }

    /**
     * Finds a debug option by its key code.
     *
     * @param keyCode The key code to search for.
     * @return The matching debug option, or null if none is found.
     */
    public static @Nullable DebugOption getByKeyCode(int keyCode) {
        for (DebugOption key : values()) {
            if (key.keyCode == keyCode) {
                return key;
            }
        }
        return null;
    }
}
