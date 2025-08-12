package com.noxcrew.noxesium.fabric.network.clientbound;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.fabric.network.CommonPackets;
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
        return CommonPackets.INSTANCE.CLIENT_OPEN_LINK;
    }
}
