package com.noxcrew.noxesium.fabric.network.serverbound;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.api.fabric.network.ServerboundNoxesiumPacket;
import com.noxcrew.noxesium.fabric.network.CommonPackets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent to the server to inform it that the client just triggered a qib interaction.
 */
public record ServerboundQibTriggeredPacket(String behavior, Type qibType, int entityId)
        implements ServerboundNoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundQibTriggeredPacket> STREAM_CODEC =
            CustomPacketPayload.codec(ServerboundQibTriggeredPacket::write, ServerboundQibTriggeredPacket::new);

    /**
     * The type of qib interaction the client triggered.
     */
    public enum Type {
        JUMP,
        INSIDE,
        ENTER,
        LEAVE
    }

    private ServerboundQibTriggeredPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readEnum(Type.class), buf.readVarInt());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(behavior);
        buf.writeEnum(qibType);
        buf.writeVarInt(entityId);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return CommonPackets.SERVER_QIB_TRIGGERED;
    }
}
