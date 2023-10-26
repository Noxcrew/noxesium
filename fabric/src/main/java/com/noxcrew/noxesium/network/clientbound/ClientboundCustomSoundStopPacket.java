package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundModule;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Sent by a server to stop a custom Noxesium sound by its id.
 */
public class ClientboundCustomSoundStopPacket extends ClientboundNoxesiumPacket {

    public final int id;

    public ClientboundCustomSoundStopPacket(FriendlyByteBuf buf) {
        super(buf.readVarInt());
        this.id = buf.readVarInt();
    }

    @Override
    public void receive(LocalPlayer player, PacketSender responseSender) {
        var manager = NoxesiumSoundModule.getInstance();
        manager.stopSound(id);
    }

    @Override
    public PacketType<?> getType() {
        return NoxesiumPackets.CLIENT_STOP_SOUND;
    }
}
