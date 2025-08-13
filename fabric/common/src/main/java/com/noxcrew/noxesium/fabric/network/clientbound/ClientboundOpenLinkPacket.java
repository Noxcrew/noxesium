package com.noxcrew.noxesium.fabric.network.clientbound;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.fabric.network.CommonPackets;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent by the server to open a link dialog on the client.
 */
public record ClientboundOpenLinkPacket(Optional<Component> text, String url) implements NoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenLinkPacket> STREAM_CODEC =
            CustomPacketPayload.codec(ClientboundOpenLinkPacket::write, ClientboundOpenLinkPacket::new);

    private ClientboundOpenLinkPacket(RegistryFriendlyByteBuf buf) {
        this(ComponentSerialization.OPTIONAL_STREAM_CODEC.decode(buf), buf.readUtf());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        ComponentSerialization.OPTIONAL_STREAM_CODEC.encode(buf, text);
        buf.writeUtf(url);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return CommonPackets.INSTANCE.CLIENT_OPEN_LINK;
    }
}
