package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundInstance;
import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundModule;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent by a server to change the volume of a sound. The interpolation time can be
 * used to fade the sound up or down over an amount of ticks
 */
public class ClientboundCustomSoundVolumePacket extends ClientboundNoxesiumPacket {
    ResourceLocation soundLocation;
    float volume;
    int interpolationTicks;

    public ClientboundCustomSoundVolumePacket(FriendlyByteBuf buf) {
        super(buf.readVarInt());
        this.soundLocation = buf.readResourceLocation();
        this.volume = buf.readFloat();
        this.interpolationTicks = buf.readVarInt();
    }

    @Override
    public void receive(LocalPlayer player, PacketSender responseSender) {
        NoxesiumSoundModule manager = NoxesiumSoundModule.getInstance();
        NoxesiumSoundInstance sound = manager.getSound(soundLocation);
        if (sound == null) return;
        sound.setVolume(this.volume, this.interpolationTicks);
    }

    @Override
    public PacketType<?> getType() {
        return NoxesiumPackets.CLIENT_SOUND_VOLUME;
    }
}
