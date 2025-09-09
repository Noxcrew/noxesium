package com.noxcrew.noxesium.core.fabric.mixin;

import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Uninitializes the protocol whenever the player instance is destroyed.
 */
@Mixin(Minecraft.class)
public class ConnectionTerminationMixin {
    @Inject(
            method = "clearClientLevel",
            at =
                    @At(
                            value = "FIELD",
                            target =
                                    "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;"))
    private void clearClientLevel(Screen screen, CallbackInfo ci) {
        var handshaker = NoxesiumMod.getInstance().getHandshaker();
        if (handshaker != null) {
            handshaker.uninitialize();
        }
    }

    @Inject(
            method = "disconnect",
            at =
                    @At(
                            value = "FIELD",
                            target =
                                    "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;"))
    private void disconnect(Screen screen, boolean bl, CallbackInfo ci) {
        var handshaker = NoxesiumMod.getInstance().getHandshaker();
        if (handshaker != null) {
            handshaker.uninitialize();
        }
    }
}
