package com.noxcrew.noxesium.core.feature;

import net.kyori.adventure.util.Index;
import org.jetbrains.annotations.Nullable;

/**
 * Provides an enum storing all available debug options to the client.
 * Each debug option has an associated keycode and translation key.
 * These are used to identify debug options to be disabled.
 */
public enum DebugOption {
    TOGGLE_PROFILER(KeyConstants.KEY_1),
    TOGGLE_FPS_CHARTS(KeyConstants.KEY_2),
    TOGGLE_NETWORK_CHARTS(KeyConstants.KEY_3),
    RELOAD_CHUNKS(KeyConstants.KEY_A),
    SHOW_HITBOXES(KeyConstants.KEY_B),
    COPY_LOCATION(KeyConstants.KEY_C),
    CLEAR_CHAT(KeyConstants.KEY_D),
    CHUNK_BOUNDARIES(KeyConstants.KEY_G),
    ADVANCED_TOOLTIPS(KeyConstants.KEY_H),
    INSPECT(KeyConstants.KEY_I),
    SMART_CULL(KeyConstants.KEY_L),
    CREATIVE_SPECTATOR(KeyConstants.KEY_N),
    PAUSE_ON_LOST_FOCUS(KeyConstants.KEY_P),
    HELP(KeyConstants.KEY_Q),
    DUMP_TEXTURES(KeyConstants.KEY_S),
    RELOAD_RESOURCES(KeyConstants.KEY_T),
    VERSION_INFO(KeyConstants.KEY_V),
    NOXESIUM_SETTINGS(KeyConstants.KEY_W),
    ESCAPE(KeyConstants.KEY_ESCAPE),
    HIDE_UI(KeyConstants.KEY_F1),
    GAME_MODE_SWITCHER(KeyConstants.KEY_F4);

    /**
     * An index of all debug options by their key codes.
     */
    public static final Index<Integer, DebugOption> KEY_CODE_INDEX =
            Index.create(DebugOption::getKeyCode, DebugOption.values());

    private final int keyCode;

    /**
     * Constructs a new debug option.
     *
     * @param keyCode The key code associated with this debug option.
     */
    DebugOption(int keyCode) {
        this.keyCode = keyCode;
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
     * Finds a debug option by its key code.
     *
     * @param keyCode The key code to search for.
     * @return The matching debug option, or null if none is found.
     */
    public static @Nullable DebugOption getByKeyCode(int keyCode) {
        return KEY_CODE_INDEX.value(keyCode);
    }
}
