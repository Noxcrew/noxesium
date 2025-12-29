package com.noxcrew.noxesium.core.fabric.mixin.feature.config;

import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into packets having additional bytes and prints out the packet that was read.
 */
@Mixin(PacketDecoder.class)
public class PacketDecoderMixin<T extends PacketListener> {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private ProtocolInfo<T> protocolInfo;

    @Inject(method = "decode", at = @At(value = "INVOKE", target = "Ljava/io/IOException;<init>(Ljava/lang/String;)V"))
    public void onExceptionCaught(
            ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list, CallbackInfo ci) {
        if (NoxesiumMod.getInstance().getConfig().printPacketExceptions) {
            // First rewind the buffer and re-create the packet
            var oldReaderIndex = byteBuf.readerIndex();
            byteBuf.setIndex(0, byteBuf.writerIndex());
            var packet = protocolInfo.codec().decode(byteBuf);

            // Secondly we print out the lingering bytes
            var out = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(out);
            LOGGER.error(
                    "Received packet with lingering bytes, packet type is {}, full received packet is {} and the following bytes were left over {}",
                    // If it's a custom payload we change the type to match the exact type
                    packet.type() == CommonPacketTypes.CLIENTBOUND_CUSTOM_PAYLOAD
                            ? ((ClientboundCustomPayloadPacket) packet)
                                    .payload()
                                    .type()
                            : packet.type(),
                    packet,
                    new String(out));

            // Set the buffer back
            byteBuf.setIndex(oldReaderIndex, byteBuf.writerIndex());
        }
    }
}
