package com.noxcrew.noxesium.core.fabric.mixin.settings;

import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into the connection class and properly prints errors to console.
 */
@Mixin(Connection.class)
public class PacketExceptionMixin {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    public void onExceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable, CallbackInfo ci) {
        if (NoxesiumMod.getInstance().getConfig().printPacketExceptions) {
            LOGGER.error("Caught exception from connection", throwable);
        }
    }
}
