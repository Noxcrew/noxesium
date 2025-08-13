package com.noxcrew.noxesium.fabric.network.serverbound;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.api.fabric.network.ServerboundNoxesiumPacket;
import com.noxcrew.noxesium.fabric.network.CommonPackets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent to the server to inform it that it just riptided. More accurate than the server
 * running equal logic to check if the player is charging the riptide and in water.
 */
public record ServerboundRiptidePacket(int slot) implements ServerboundNoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundRiptidePacket> STREAM_CODEC =
            CustomPacketPayload.codec(ServerboundRiptidePacket::write, ServerboundRiptidePacket::new);

    private ServerboundRiptidePacket(RegistryFriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(slot);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return CommonPackets.SERVER_RIPTIDE;
    }
}
