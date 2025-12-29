package com.noxcrew.noxesium.core.feature;

/**
 * Provides an enum with all UI elements that can be resized by the client,
 * or have its size restricted by the server.
 */
public enum GuiElement {
    BOSS_BAR,
    SCOREBOARD,
    TAB_LIST,
    TITLE,
    ACTION_BAR,
    CHAT,
    HOTBAR,
    ACTIVE_EFFECTS,
    /**
     * The subtitles shown in the bottom right corner of the screen,
     * recently renamed to Closed Captions (CC). The subtitle in the middle
     * of the screen is a part of {@link GuiElement#TITLE}.
     */
    SUBTITLES,
    /**
     * The custom on-screen map added by Noxesium.
     */
    MAP,
}
