package com.noxcrew.noxesium.legacy.paper.api.network.serverbound

import com.noxcrew.noxesium.legacy.paper.api.network.NoxesiumPackets
import net.minecraft.network.RegistryFriendlyByteBuf
import org.bukkit.entity.Player

/**
 * Sent to the server to inform it that the client just triggered a qib interaction.
 */
public data class ServerboundMouseButtonClickPacket(
    public val action: Action,
    public val button: Button,
) : ServerboundNoxesiumPacket(NoxesiumPackets.SERVER_MOUSE_BUTTON_CLICK) {
    public enum class Action {
        PRESS_DOWN,
        RELEASE,
    }

    public enum class Button {
        LEFT,
        MIDDLE,
        RIGHT,
    }

    public constructor(buffer: RegistryFriendlyByteBuf, player: Player, protocolVersion: Int) : this(
        buffer.readEnum(Action::class.java),
        buffer.readEnum(Button::class.java),
    )
}
