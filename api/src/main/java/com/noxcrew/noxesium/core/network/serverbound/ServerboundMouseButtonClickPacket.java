package com.noxcrew.noxesium.core.network.serverbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;

/**
 * Sent to the server to inform it of which mouse button the player pressed.
 */
public record ServerboundMouseButtonClickPacket(Action action, Button button) implements NoxesiumPacket {
    /**
     * The different available mouse actions.
     */
    public enum Action {
        PRESS_DOWN,
        RELEASE,
    }

    /**
     * The different available mouse buttons.
     */
    public enum Button {
        LEFT,
        MIDDLE,
        RIGHT,
    }
}
