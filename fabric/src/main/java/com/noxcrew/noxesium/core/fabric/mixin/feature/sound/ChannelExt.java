package com.noxcrew.noxesium.core.fabric.mixin.feature.sound;

import com.mojang.blaze3d.audio.Channel;
import javax.sound.sampled.AudioFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Channel.class)
public interface ChannelExt {

    @Accessor("source")
    int getSource();

    @Invoker("calculateBufferSize")
    static int invokeCalculateBufferSize(AudioFormat audioFormat, int i) {
        throw new AssertionError("Unimplemented");
    }
}
