package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundInstance;
import com.noxcrew.noxesium.feature.sounds.NoxesiumSoundModule;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * Sent by a server to start a Noxesium custom sound. If a sound with the same resource location
 * is already playing, that sound will be stopped.
 */
public class ClientboundCustomSoundStartPacket extends ClientboundNoxesiumPacket {
    private final ResourceLocation sound;
    private final int id;
    private final SoundSource source;
    private final int x;
    private final int y;
    private final int z;
    private final boolean looping;
    private final float volume;
    private final float pitch;
    private final float startOffset;

    public ClientboundCustomSoundStartPacket(FriendlyByteBuf buf) {
        super(buf.readVarInt());
        this.sound = buf.readResourceLocation();
        this.id = buf.readVarInt();
        this.source = buf.readEnum(SoundSource.class);
        this.x = buf.readVarInt();
        this.y = buf.readVarInt();
        this.z = buf.readVarInt();
        this.looping = buf.readBoolean();
        this.volume = buf.readFloat();
        this.pitch = buf.readFloat();
        this.startOffset = buf.readFloat();
    }

    @Override
    public void receive(LocalPlayer player, PacketSender responseSender) {
        NoxesiumSoundModule manager = NoxesiumSoundModule.getInstance();
        NoxesiumSoundInstance instance = new NoxesiumSoundInstance(
                sound,
                source,
                new Vec3(x, y, z),
                volume,
                pitch,
                looping,
                startOffset
        );
        manager.play(id, instance);
    }

    @Override
    public PacketType<?> getType() {
        return NoxesiumPackets.CLIENT_START_SOUND;
    }
}
