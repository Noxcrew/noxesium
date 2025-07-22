package com.noxcrew.noxesium.network.serverbound;

import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.NoxesiumPayloadType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent to the server to inform it of which mouse button the player pressed.
 */
public record ServerboundMouseButtonClickPacket(Action action, Button button) implements ServerboundNoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundMouseButtonClickPacket> STREAM_CODEC =
            CustomPacketPayload.codec(ServerboundMouseButtonClickPacket::write, ServerboundMouseButtonClickPacket::new);

    public enum Action {
        PRESS_DOWN,
        RELEASE,
    }

    public enum Button {
        LEFT,
        MIDDLE,
        RIGHT,
    }

    private ServerboundMouseButtonClickPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readEnum(Action.class), buf.readEnum(Button.class));
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeEnum(button);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return NoxesiumPackets.SERVER_MOUSE_BUTTON_CLICK;
    }
}