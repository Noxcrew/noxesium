package com.noxcrew.noxesium.core.network.serverbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import net.kyori.adventure.key.Key;

/**
 * Sent to the server to inform it that the client just triggered a qib interaction.
 */
public record ServerboundQibTriggeredPacket(Key behavior, Type qibType, int entityId) implements NoxesiumPacket {
    /**
     * The type of qib interaction the client triggered.
     */
    public enum Type {
        JUMP,
        INSIDE,
        ENTER,
        LEAVE,
        ATTACK_BEHAVIOR,
        USE_BEHAVIOR
    }
}
