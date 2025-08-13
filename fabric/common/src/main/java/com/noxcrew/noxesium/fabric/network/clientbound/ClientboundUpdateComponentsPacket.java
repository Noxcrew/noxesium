package com.noxcrew.noxesium.fabric.network.clientbound;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.api.fabric.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.fabric.network.CommonPackets;
import com.noxcrew.noxesium.fabric.network.NoxesiumComponentPatch;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Changes the values of entity or game components on the client.
 */
public record ClientboundUpdateComponentsPacket(Optional<Integer> entityId, boolean reset, NoxesiumComponentPatch patch)
        implements NoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateComponentsPacket> STREAM_CODEC =
            CustomPacketPayload.codec(ClientboundUpdateComponentsPacket::write, ClientboundUpdateComponentsPacket::new);

    private ClientboundUpdateComponentsPacket(RegistryFriendlyByteBuf buf) {
        this(buf, buf.readOptional(FriendlyByteBuf::readVarInt), buf.readBoolean());
    }

    private ClientboundUpdateComponentsPacket(RegistryFriendlyByteBuf buf, Optional<Integer> entityId, boolean reset) {
        this(
                entityId,
                reset,
                NoxesiumComponentPatch.streamCodec(
                                entityId.isPresent()
                                        ? NoxesiumRegistries.ENTITY_COMPONENTS
                                        : NoxesiumRegistries.GAME_COMPONENTS)
                        .decode(buf));
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeOptional(entityId, FriendlyByteBuf::writeVarInt);
        buf.writeBoolean(reset);
        NoxesiumComponentPatch.streamCodec(
                        entityId.isPresent()
                                ? NoxesiumRegistries.ENTITY_COMPONENTS
                                : NoxesiumRegistries.GAME_COMPONENTS)
                .encode(buf, patch);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return CommonPackets.CLIENT_UPDATE_COMPONENTS;
    }
}
