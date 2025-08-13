package com.noxcrew.noxesium.fabric.network.clientbound;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.fabric.NoxesiumMod;
import com.noxcrew.noxesium.fabric.feature.entity.ExtraEntityDataModule;
import com.noxcrew.noxesium.fabric.network.CommonPackets;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Changes the value of extra entity data on a target entity.
 */
public record ClientboundSetExtraEntityDataPacket(int entityId, IntList indices, List<Object> values)
        implements NoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSetExtraEntityDataPacket> STREAM_CODEC =
            CustomPacketPayload.codec(
                    ClientboundSetExtraEntityDataPacket::write, ClientboundSetExtraEntityDataPacket::new);

    private ClientboundSetExtraEntityDataPacket(RegistryFriendlyByteBuf buf) {
        this(buf, buf.readVarInt(), buf.readIntIdList());
    }

    private ClientboundSetExtraEntityDataPacket(RegistryFriendlyByteBuf buf, int entityId, IntList indices) {
        this(
                entityId,
                indices,
                ClientboundChangeServerRulesPacket.readValues(
                        NoxesiumMod.getInstance().getFeature(ExtraEntityDataModule.class), buf, indices));
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        ClientboundChangeServerRulesPacket.write(
                NoxesiumMod.getInstance().getFeature(ExtraEntityDataModule.class), buf, indices, values);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return CommonPackets.INSTANCE.CLIENT_CHANGE_EXTRA_ENTITY_DATA;
    }
}
