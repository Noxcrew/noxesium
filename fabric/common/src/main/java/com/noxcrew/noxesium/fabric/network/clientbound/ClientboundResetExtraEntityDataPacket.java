package com.noxcrew.noxesium.fabric.network.clientbound;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.fabric.network.CommonPackets;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Resets the stored value for extra data on an entity.
 */
public record ClientboundResetExtraEntityDataPacket(int entityId, IntList indices) implements NoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundResetExtraEntityDataPacket> STREAM_CODEC =
            CustomPacketPayload.codec(
                    ClientboundResetExtraEntityDataPacket::write, ClientboundResetExtraEntityDataPacket::new);

    private ClientboundResetExtraEntityDataPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readIntIdList());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeIntIdList(indices);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return CommonPackets.INSTANCE.CLIENT_RESET_EXTRA_ENTITY_DATA;
    }
}
