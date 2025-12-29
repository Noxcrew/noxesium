package com.noxcrew.noxesium.core.fabric.mixin.network;

import com.noxcrew.noxesium.api.network.ConnectionProtocolType;
import com.noxcrew.noxesium.api.network.NoxesiumErrorReason;
import com.noxcrew.noxesium.api.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Uninitializes the protocol whenever the client disconnects, which can happen during
 * either the configuration or play phases.
 */
@Mixin(Minecraft.class)
public class ConnectionTerminationMixin {
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At(value = "RETURN"))
    private void disconnect(Screen screen, boolean bl, boolean bl2, CallbackInfo ci) {
        var handshaker = NoxesiumMod.getInstance().getHandshaker();
        if (handshaker != null) {
            handshaker.uninitialize(NoxesiumErrorReason.DISCONNECT);
        }

        // Un-set the current protocol!
        NoxesiumServerboundNetworking.getInstance().setConfiguredProtocol(ConnectionProtocolType.NONE);
    }
}
