package com.noxcrew.noxesium.network.serverbound;

import com.noxcrew.noxesium.api.protocol.ClientSettings;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.NoxesiumPayloadType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent to the server to inform it about various settings configured by the client,
 * mostly geared towards the size of their GUI and other visual-related settings.
 */
public record ServerboundClientSettingsPacket(ClientSettings settings) implements ServerboundNoxesiumPacket {
    public static final StreamCodec<FriendlyByteBuf, ServerboundClientSettingsPacket> STREAM_CODEC = CustomPacketPayload.codec(ServerboundClientSettingsPacket::write, ServerboundClientSettingsPacket::new);
    public static final NoxesiumPayloadType<ServerboundClientSettingsPacket> TYPE = NoxesiumPackets.server("client_settings", STREAM_CODEC);

    private ServerboundClientSettingsPacket(FriendlyByteBuf buf) {
        this(new ClientSettings(
                buf.readVarInt(),
                buf.readDouble(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readDouble()
        ));
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(settings.configuredGuiScale());
        buf.writeDouble(settings.trueGuiScale());
        buf.writeVarInt(settings.width());
        buf.writeVarInt(settings.height());
        buf.writeBoolean(settings.enforceUnicode());
        buf.writeBoolean(settings.touchScreenMode());
        buf.writeDouble(settings.notificationDisplayTime());
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return TYPE;
    }
}
