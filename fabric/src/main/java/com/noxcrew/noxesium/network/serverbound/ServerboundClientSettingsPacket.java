package com.noxcrew.noxesium.network.serverbound;

import com.noxcrew.noxesium.network.NoxesiumPackets;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Sent to the server to inform it about various settings configured by the client,
 * mostly geared towards the size of their GUI and other visual-related settings.
 */
public class ServerboundClientSettingsPacket extends ServerboundNoxesiumPacket {

    private final int configuredGuiScale;
    private final double trueGuiScale;
    private final int width;
    private final int height;
    private final boolean enforceUnicode;
    private final boolean touchScreenMode;
    private final double notificationDisplayTime;

    public ServerboundClientSettingsPacket(
            int configuredGuiScale,
            double trueGuiScale,
            int width,
            int height,
            boolean enforceUnicode,
            boolean touchScreenMode,
            double notificationDisplayTime
    ) {
        super(1);
        this.configuredGuiScale = configuredGuiScale;
        this.trueGuiScale = trueGuiScale;
        this.width = width;
        this.height = height;
        this.enforceUnicode = enforceUnicode;
        this.touchScreenMode = touchScreenMode;
        this.notificationDisplayTime = notificationDisplayTime;
    }

    @Override
    public void serialize(FriendlyByteBuf buffer) {
        buffer.writeVarInt(configuredGuiScale);
        buffer.writeDouble(trueGuiScale);
        buffer.writeVarInt(width);
        buffer.writeVarInt(height);
        buffer.writeBoolean(enforceUnicode);
        buffer.writeBoolean(touchScreenMode);
        buffer.writeDouble(notificationDisplayTime);
    }

    @Override
    public PacketType<?> getType() {
        return NoxesiumPackets.SERVER_CLIENT_SETTINGS;
    }
}
