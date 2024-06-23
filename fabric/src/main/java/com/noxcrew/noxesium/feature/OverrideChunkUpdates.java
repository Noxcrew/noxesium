package com.noxcrew.noxesium.feature;

import com.noxcrew.noxesium.NoxesiumModule;

/**
 * Manages overriding chunk updates to be blocking while in Hole in the Wall.
 */
public class OverrideChunkUpdates implements NoxesiumModule {

    private boolean overriding = false;
    private String game = "";

    /**
     * Returns whether this module should currently be enabled.
     */
    public boolean shouldOverride() {
        return overriding;
    }

    /**
     * Returns if the current game is Bingo but Fast.
     */
    public boolean isPlayingBingo() {
        return game.equals("bingo_but_fast");
    }

    /**
     * Returns the id of the currently played game.
     */
    public String getCurrentGame() {
        return game;
    }

    /**
     * Updates whether this module is currently enabled.
     */
    public void updateState(String game) {
        this.game = game;
        this.overriding = game.equals("hole_in_the_wall");
    }

    @Override
    public void onQuitServer() {
        overriding = false;
        game = "";
    }
}
