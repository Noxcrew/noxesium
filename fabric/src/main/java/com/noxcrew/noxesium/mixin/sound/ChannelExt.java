package com.noxcrew.noxesium.mixin.sound;

import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.sounds.AudioStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Channel.class)
public interface ChannelExt {

    @Accessor("source")
    int getSource();

    @Accessor("streamingBufferSize")
    int getStreamingBufferSize();

    @Accessor("stream")
    AudioStream getStream();

}
