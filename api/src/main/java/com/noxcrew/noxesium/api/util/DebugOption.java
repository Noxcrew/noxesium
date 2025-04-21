package com.noxcrew.noxesium.api.util;

import org.jetbrains.annotations.Nullable;

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

    DebugOption(int keyCode, @Nullable String helpTranslationKey) {
        this.keyCode = keyCode;
        this.translationKey = helpTranslationKey;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public @Nullable String getTranslationKey() {
        return translationKey;
    }

    public static @Nullable DebugOption getByTranslationKey(String translationKey) {
        for (DebugOption key : values()) {
            if (key.translationKey != null && key.translationKey.equals(translationKey)) {
                return key;
            }
        }
        return null;
    }

    public static @Nullable DebugOption getByKeyCode(int keyCode) {
        for (DebugOption key : values()) {
            if (key.keyCode == keyCode) {
                return key;
            }
        }
        return null;
    }
}
