package com.noxcrew.noxesium.network.serverbound;

import com.noxcrew.noxesium.api.protocol.ClientSettings;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Sent to the server to inform it about various settings configured by the client,
 * mostly geared towards the size of their GUI and other visual-related settings.
 */
public class ServerboundClientSettingsPacket extends ServerboundNoxesiumPacket {

    private final ClientSettings settings;

    public ServerboundClientSettingsPacket(ClientSettings settings) {
        super(1);
        this.settings = settings;
    }

    @Override
    public void serialize(FriendlyByteBuf buffer) {
        buffer.writeVarInt(settings.configuredGuiScale());
        buffer.writeDouble(settings.trueGuiScale());
        buffer.writeVarInt(settings.width());
        buffer.writeVarInt(settings.height());
        buffer.writeBoolean(settings.enforceUnicode());
        buffer.writeBoolean(settings.touchScreenMode());
        buffer.writeDouble(settings.notificationDisplayTime());
    }

    @Override
    public PacketType<?> getType() {
        return NoxesiumPackets.SERVER_CLIENT_SETTINGS;
    }
}
