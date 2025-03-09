package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.network.NoxesiumPacket;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.NoxesiumPayloadType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;

/**
 * Sent by the server to open a link dialog on the client.
 */
public record ClientboundOpenLinkPacket(@Nullable Component text, String url) implements NoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenLinkPacket> STREAM_CODEC =
            CustomPacketPayload.codec(ClientboundOpenLinkPacket::write, ClientboundOpenLinkPacket::new);

    private ClientboundOpenLinkPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readBoolean() ? ComponentSerialization.STREAM_CODEC.decode(buf) : null, buf.readUtf());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        if (text != null) {
            buf.writeBoolean(true);
            ComponentSerialization.STREAM_CODEC.encode(buf, text);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeUtf(url);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return NoxesiumPackets.CLIENT_OPEN_LINK;
    }
}
