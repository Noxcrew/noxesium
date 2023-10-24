package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundModule;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Sent by a server to change the volume of a sound. The interpolation time can be
 * used to fade the sound up or down over an amount of ticks
 */
public class ClientboundCustomSoundModifyPacket extends ClientboundNoxesiumPacket {

    public final int id;
    public final float volume;
    /**
     * An optional volume to start the interpolation from. If absent the current
     * volume of the sound is used instead.
     */
    public final Float startVolume;
    public final int interpolationTicks;

    public ClientboundCustomSoundModifyPacket(FriendlyByteBuf buf) {
        super(buf.readVarInt());
        this.id = buf.readVarInt();
        this.volume = buf.readFloat();
        this.interpolationTicks = buf.readVarInt();
        if (buf.readBoolean()) {
            this.startVolume = buf.readFloat();
        } else {
            this.startVolume = null;
        }
    }

    @Override
    public void receive(LocalPlayer player, PacketSender responseSender) {
        var manager = NoxesiumSoundModule.getInstance();
        var sound = manager.getSound(id);
        if (sound == null) return;

        sound.setVolume(this.volume, this.startVolume, this.interpolationTicks);
    }

    @Override
    public PacketType<?> getType() {
        return NoxesiumPackets.CLIENT_MODIFY_SOUND;
    }
}
