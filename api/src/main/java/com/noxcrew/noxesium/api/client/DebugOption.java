package com.noxcrew.noxesium.api.client;

import net.kyori.adventure.util.Index;
import org.jetbrains.annotations.Nullable;

/**
 * Provides an enum storing all available debug options to the client.
 * Each debug option has an associated keycode and translation key.
 * These are used to identify debug options to be disabled.
 */
public enum DebugOption {
    TOGGLE_PROFILER(KeyConstants.KEY_1, null),
    TOGGLE_FPS_CHARTS(KeyConstants.KEY_2, null),
    TOGGLE_NETWORK_CHARTS(KeyConstants.KEY_3, null),

    RELOAD_CHUNKS(KeyConstants.KEY_A, "debug.reload_chunks.help"),
    SHOW_HITBOXES(KeyConstants.KEY_B, "debug.show_hitboxes.help"),
    COPY_LOCATION(KeyConstants.KEY_C, "debug.copy_location.help"),
    CLEAR_CHAT(KeyConstants.KEY_D, "debug.clear_chat.help"),
    CHUNK_BOUNDARIES(KeyConstants.KEY_G, "debug.chunk_boundaries.help"),
    ADVANCED_TOOLTIPS(KeyConstants.KEY_H, "debug.advanced_tooltips.help"),
    INSPECT(KeyConstants.KEY_I, "debug.inspect.help"),
    SMART_CULL(KeyConstants.KEY_L, "debug.profiling.help"),
    CREATIVE_SPECTATOR(KeyConstants.KEY_N, "debug.creative_spectator.help"),
    PAUSE_ON_LOST_FOCUS(KeyConstants.KEY_P, "debug.pause_focus.help"),
    HELP(KeyConstants.KEY_Q, "debug.help.help"),
    DUMP_TEXTURES(KeyConstants.KEY_S, "debug.dump_dynamic_textures.help"),
    RELOAD_RESOURCES(KeyConstants.KEY_T, "debug.reload_resourcepacks.help"),
    VERSION_INFO(KeyConstants.KEY_V, "debug.version.help"),
    NOXESIUM_SETTINGS(KeyConstants.KEY_W, "debug.noxesium_settings.help"),
    ESCAPE(KeyConstants.KEY_ESCAPE, "debug.pause.help"),
    GAME_MODE_SWITCHER(KeyConstants.KEY_F4, "debug.gamemodes.help");

    /**
     * An index of all debug options by their translation keys.
     */
    public static final Index<String, DebugOption> TRANSLATION_KEY_INDEX =
            Index.create(DebugOption::getTranslationKey, DebugOption.values());

    /**
     * An index of all debug options by their key codes.
     */
    public static final Index<Integer, DebugOption> KEY_CODE_INDEX =
            Index.create(DebugOption::getKeyCode, DebugOption.values());

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
        return TRANSLATION_KEY_INDEX.value(translationKey);
    }

    /**
     * Finds a debug option by its key code.
     *
     * @param keyCode The key code to search for.
     * @return The matching debug option, or null if none is found.
     */
    public static @Nullable DebugOption getByKeyCode(int keyCode) {
        return KEY_CODE_INDEX.value(keyCode);
    }
}
