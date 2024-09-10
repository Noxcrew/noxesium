package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.entity.ExtraEntityDataModule;
import com.noxcrew.noxesium.network.NoxesiumPacket;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.NoxesiumPayloadType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/**
 * Changes the value of extra entity data on a target entity.
 */
public record ClientboundSetExtraEntityDataPacket(int entityId, IntList indices, List<Object> values) implements NoxesiumPacket {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSetExtraEntityDataPacket> STREAM_CODEC = CustomPacketPayload.codec(ClientboundSetExtraEntityDataPacket::write, ClientboundSetExtraEntityDataPacket::new);

    private ClientboundSetExtraEntityDataPacket(FriendlyByteBuf buf) {
        this(buf, buf.readVarInt(), buf.readIntIdList());
    }

    private ClientboundSetExtraEntityDataPacket(FriendlyByteBuf buf, int entityId, IntList indices) {
        this(entityId, indices, ClientboundChangeServerRulesPacket.readValues(NoxesiumMod.getInstance().getModule(ExtraEntityDataModule.class), buf, indices));
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        ClientboundChangeServerRulesPacket.write(NoxesiumMod.getInstance().getModule(ExtraEntityDataModule.class), buf, indices, values);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return NoxesiumPackets.CLIENT_CHANGE_EXTRA_ENTITY_DATA;
    }
}