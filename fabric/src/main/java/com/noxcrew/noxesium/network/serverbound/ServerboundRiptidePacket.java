package com.noxcrew.noxesium.network.serverbound;

import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.NoxesiumPayloadType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

/**
 * Sent to the server to inform it that it just riptided. More accurate than the server
 * running equal logic to check if the player is charging the riptide and in water.
 */
public record ServerboundRiptidePacket(int slot) implements ServerboundNoxesiumPacket {
    public static final StreamCodec<FriendlyByteBuf, ServerboundRiptidePacket> STREAM_CODEC = CustomPacketPayload.codec(ServerboundRiptidePacket::write, ServerboundRiptidePacket::new);

    private ServerboundRiptidePacket(FriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(slot);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return NoxesiumPackets.RIPTIDE;
    }
}
